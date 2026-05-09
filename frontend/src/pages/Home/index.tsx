import React, { useEffect, useMemo, useState } from 'react'
import { Card, Col, Empty, List, message, Row, Spin, Statistic, Typography } from 'antd'
import { CarOutlined, EnvironmentOutlined, HomeOutlined } from '@ant-design/icons'
import { AddressDistribution, expressApi } from '../../api/express'

const { Title, Text } = Typography

interface ChartItem {
  name: string
  value: number
}

interface PieChartProps {
  data: ChartItem[]
}

const COLORS = ['#1677ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96', '#fa8c16']

const toChartData = (stats: Record<string, number>): ChartItem[] => {
  return Object.entries(stats)
    .map(([name, value]) => ({ name, value }))
    .filter(item => item.value > 0)
    .sort((a, b) => b.value - a.value)
}

const buildPieSegments = (data: ChartItem[]) => {
  const total = data.reduce((sum, item) => sum + item.value, 0)
  let offset = 25

  return data.map((item, index) => {
    const percent = total > 0 ? (item.value / total) * 100 : 0
    const segment = {
      ...item,
      percent,
      color: COLORS[index % COLORS.length],
      dashArray: `${percent} ${100 - percent}`,
      dashOffset: offset,
    }
    offset -= percent
    return segment
  })
}

const PieChart: React.FC<PieChartProps> = ({ data }) => {
  const segments = useMemo(() => buildPieSegments(data), [data])
  const total = data.reduce((sum, item) => sum + item.value, 0)

  if (total === 0) {
    return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无地址数据" />
  }

  return (
    <div style={{ display: 'flex', gap: 24, alignItems: 'center', flexWrap: 'wrap' }}>
      <svg width="220" height="220" viewBox="0 0 42 42" aria-label="收件地址分布饼图">
        <circle cx="21" cy="21" r="15.915" fill="transparent" stroke="#f0f0f0" strokeWidth="6" />
        {segments.map(segment => (
          <circle
            key={segment.name}
            cx="21"
            cy="21"
            r="15.915"
            fill="transparent"
            stroke={segment.color}
            strokeWidth="6"
            strokeDasharray={segment.dashArray}
            strokeDashoffset={segment.dashOffset}
          />
        ))}
        <text x="21" y="20" textAnchor="middle" fontSize="4" fill="#262626" fontWeight="600">
          {total}
        </text>
        <text x="21" y="25" textAnchor="middle" fontSize="2.5" fill="#8c8c8c">
          条记录
        </text>
      </svg>

      <List
        size="small"
        dataSource={segments.slice(0, 8)}
        style={{ minWidth: 220, flex: 1 }}
        renderItem={item => (
          <List.Item>
            <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ width: 10, height: 10, borderRadius: 2, background: item.color }} />
              <span>{item.name}</span>
            </span>
            <Text type="secondary">{item.value} 条 / {item.percent.toFixed(1)}%</Text>
          </List.Item>
        )}
      />
    </div>
  )
}

const Home: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [distribution, setDistribution] = useState<AddressDistribution>({
    provinceStats: {},
    cityStats: {},
    matchedCount: 0,
    unmatchedCount: 0,
  })

  const provinceData = useMemo(() => toChartData(distribution.provinceStats), [distribution.provinceStats])
  const cityData = useMemo(() => toChartData(distribution.cityStats), [distribution.cityStats])

  useEffect(() => {
    const fetchDistribution = async () => {
      setLoading(true)
      try {
        const res = await expressApi.getAddressDistribution() as unknown as AddressDistribution
        setDistribution(res)
      } catch (error: any) {
        message.error(error.message || '获取快递地址分布失败')
      } finally {
        setLoading(false)
      }
    }

    fetchDistribution()
  }, [])

  return (
    <div style={{ padding: 24 }}>
      <Title level={3} style={{ marginBottom: 4 }}>快递分析</Title>
      <Text type="secondary">按收件地址统计省份和城市分布</Text>

      <Spin spinning={loading}>
        <Row gutter={[16, 16]} style={{ marginTop: 24, marginBottom: 24 }}>
          <Col xs={24} md={8}>
            <Card>
              <Statistic title="已识别地址" value={distribution.matchedCount} suffix="条" prefix={<EnvironmentOutlined />} />
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card>
              <Statistic title="省份数量" value={provinceData.length} suffix="个" prefix={<HomeOutlined />} />
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card>
              <Statistic title="城市数量" value={cityData.length} suffix="个" prefix={<CarOutlined />} />
            </Card>
          </Col>
        </Row>

        <Row gutter={[16, 16]}>
          <Col xs={24} xl={12}>
            <Card title="收件省份分布">
              <PieChart data={provinceData} />
            </Card>
          </Col>
          <Col xs={24} xl={12}>
            <Card title="收件城市分布">
              <PieChart data={cityData} />
            </Card>
          </Col>
        </Row>
      </Spin>
    </div>
  )
}

export default Home
