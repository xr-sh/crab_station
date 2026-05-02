import React, { useState, useEffect, useMemo } from 'react'
import {
  Table,
  Button,
  Space,
  Card,
  Typography,
  message,
  Popconfirm,
  Select,
  Statistic,
  Row,
  Col,
  Empty,
  Grid,
  DatePicker,
  Cascader,
} from 'antd'
import {
  UploadOutlined,
  ClearOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { expressApi, ExpressAnalysis, Statistics, EXPRESS_CATEGORIES } from '../../api/express'
import ImportModal from './components/ImportModal'
import { getAreaOptionsCached, getFullAddress } from '../../utils/chinaDivision'

const { Title } = Typography
const { useBreakpoint } = Grid
const { RangePicker } = DatePicker

const Express: React.FC = () => {
  const [data, setData] = useState<ExpressAnalysis[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [selectedCategory, setSelectedCategory] = useState<string | undefined>('顺丰')
  const [modalVisible, setModalVisible] = useState(false)
  const [statistics, setStatistics] = useState<Statistics>({
    totalRecords: 0,
    fileCount: 0,
    columnCount: 0,
    categoryStats: {},
    averageFee: 0,
  })
  const [sorter, setSorter] = useState<{
    field: string | null,
    order: 'ascend' | 'descend' | null
  }>({
    field: null,
    order: null,
  })
  // 搜索条件
  const [receiverAddressArr, setReceiverAddressArr] = useState<string[]>([])
  const [sentTimeRange, setSentTimeRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null] | null>(null)
  
  // 省市区数据（缓存）
  const areaOptions = useMemo(() => getAreaOptionsCached(), [])
  
  const screens = useBreakpoint()
  const isMobile = !screens.md

  // 获取统计信息
  const fetchStatistics = async () => {
    try {
      const res: any = await expressApi.getStatistics()
      setStatistics(res)
    } catch (error: any) {
      console.error('获取统计失败', error)
    }
  }

  // 获取数据列表
  const fetchData = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      // 构建排序参数（使用数据库列名）
      let sortBy = 'imported_at'
      let sortDirection = 'DESC'
      
      if (sorter.field && sorter.order) {
        // 映射前端字段名到数据库列名
        const fieldMapping: Record<string, string> = {
          duration: 'duration_hours',
          sentTime: 'sentTime',
          receivedTime: 'receivedTime',
        }
        sortBy = fieldMapping[sorter.field] || sorter.field
        sortDirection = sorter.order === 'ascend' ? 'ASC' : 'DESC'
      }
      
      // 构建搜索参数
      const params: any = {
        page,
        size,
        sortBy,
        sortDirection,
        category: selectedCategory,
      }
      
      // 收件地址搜索（将选中的省市区数组转为完整地址字符串）
      const receiverAddress = getFullAddress(receiverAddressArr)
      if (receiverAddress) {
        params.receiverAddress = receiverAddress
      }
      
      // 寄件时间范围搜索
      if (sentTimeRange && sentTimeRange[0] && sentTimeRange[1]) {
        params.sentTimeStart = sentTimeRange[0].format('YYYY-MM-DD')
        params.sentTimeEnd = sentTimeRange[1].format('YYYY-MM-DD')
      }
      
      const res = await expressApi.getList(params) as any
      
      setData(res.content)
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.totalElements,
      })
    } catch (error: any) {
      message.error(error.message || '获取数据失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchStatistics()
    fetchData()
  }, [])

  useEffect(() => {
    fetchData(0, pagination.pageSize)
  }, [selectedCategory, sorter, receiverAddressArr, sentTimeRange])

  // 刷新数据
  const handleRefresh = () => {
    fetchStatistics()
    fetchData(pagination.current - 1, pagination.pageSize)
  }

  // 清空所有数据
  const handleClearAll = async () => {
    try {
      await expressApi.clearAll()
      message.success('已清空所有数据')
      handleRefresh()
    } catch (error: any) {
      message.error(error.message || '清空失败')
    }
  }

  // 重置搜索条件
  const handleResetSearch = () => {
    setReceiverAddressArr([])
    setSentTimeRange(null)
    setSelectedCategory('顺丰')
    fetchData(0, pagination.pageSize)
  }

  // 搜索按钮
  const handleSearch = () => {
    fetchData(0, pagination.pageSize)
  }

  // 导入成功回调
  const handleImportSuccess = () => {
    handleRefresh()
  }

  // 表格变化
  const handleTableChange = (newPagination: any, _filters: any, newSorter: any) => {
    // 更新排序状态
    if (newSorter && newSorter.field) {
      setSorter({
        field: newSorter.field,
        order: newSorter.order || null,
      })
    } else {
      setSorter({ field: null, order: null })
    }
    
    // 更新分页
    fetchData(newPagination.current - 1, newPagination.pageSize)
  }

  // 动态生成表格列
  const generateColumns = () => {
    // 固定列顺序
    const columnConfig = [
      { title: '寄件时间', key: 'sentTime', width: 150 },
      { title: '签收时间', key: 'receivedTime', width: 150 },
      { title: '时效', key: 'duration', width: 120, sortable: true },
      { title: '收件人', key: 'receiver', width: 120 },
      { title: '收件地址', key: 'receiverAddress', width: 200, ellipsis: true },
      { title: '计费重量', key: 'weight', width: 100 },
      { title: '运单号', key: 'trackingNo', width: 150 },
      { title: '运费', key: 'fee', width: 100 },
    ]

    // 字段名映射（支持多种可能的字段名）
    const fieldMappings: Record<string, string[]> = {
      sentTime: ['寄件时间', '寄件日期', '寄件', '发货时间', '发货日期', '发出时间', '发出日期'],
      receivedTime: ['签收时间', '签收日期', '签收', '收货时间', '收货日期'],
      receiver: ['收件人', '收件人姓名', '收货人', '收货人姓名'],
      receiverAddress: ['收件地址', '收件人地址', '收货地址', '收货人地址', '收件人详细地址'],
      weight: ['计费重量', '重量', '收费重量', '实际重量'],
      trackingNo: ['运单号', '快递单号', '单号', '物流单号', '订单号'],
      fee: ['运费', '费用', '快递费', '物流费', '快递费用'],
    }

    return columnConfig.map(col => ({
      title: col.title,
      key: col.key,
      dataIndex: col.key,
      width: col.width,
      ellipsis: col.ellipsis,
      sorter: col.sortable ? true : undefined,
      sortOrder: col.sortable && sorter.field === col.key ? sorter.order : undefined,
      render: (_: any, record: ExpressAnalysis) => {
        // 时效列直接使用后端计算的值
        if (col.key === 'duration') {
          return record.duration || '-'
        }

        // 其他列从 dynamicFields 中查找对应字段
        const possibleFieldNames = fieldMappings[col.key] || [col.title]
        const fields = record.dynamicFields || {}
        
        for (const fieldName of possibleFieldNames) {
          const value = fields[fieldName]
          if (value !== undefined && value !== null) {
            return String(value)
          }
        }
        
        return '-'
      },
    }))
  }

  return (
    <div style={{ padding: isMobile ? 16 : 24 }}>
      {/* 统计卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="总记录数"
              value={statistics.totalRecords}
              suffix="条"
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="顺丰"
              value={statistics.categoryStats?.['顺丰'] || 0}
              suffix="条"
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="京东"
              value={statistics.categoryStats?.['京东'] || 0}
              suffix="条"
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="平均运费"
              value={statistics.averageFee || 0}
              suffix="元"
              precision={2}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', flexDirection: isMobile ? 'column' : 'row', justifyContent: 'space-between', gap: isMobile ? 12 : 0 }}>
            <Title level={4} style={{ margin: 0 }}>快递分析</Title>
            <Space wrap>
              <Button icon={<UploadOutlined />} type="primary" onClick={() => setModalVisible(true)}>
                导入数据
              </Button>
              <Button icon={<ReloadOutlined />} onClick={handleRefresh}>
                刷新
              </Button>
              <Popconfirm
                title="确认清空"
                description="确定要清空所有数据吗？此操作不可恢复！"
                onConfirm={handleClearAll}
                okText="确定"
                cancelText="取消"
              >
                <Button danger icon={<ClearOutlined />}>
                  清空数据
                </Button>
              </Popconfirm>
            </Space>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <span>筛选类别：</span>
            <Select
              placeholder="选择快递类别"
              allowClear
              style={{ width: isMobile ? '100%' : 150 }}
              value={selectedCategory}
              onChange={setSelectedCategory}
              options={EXPRESS_CATEGORIES.map(name => ({ value: name, label: name }))}
            />
          </Space>
        </div>

        {/* 搜索区域 */}
        <div style={{ marginBottom: 16, padding: '16px', background: '#fafafa', borderRadius: 8 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Cascader
              options={areaOptions}
              value={receiverAddressArr}
              onChange={(value) => setReceiverAddressArr(value as string[])}
              placeholder="选择收件地址（省/市/区）"
              style={{ width: isMobile ? '100%' : 280 }}
              showSearch={{
                filter: (inputValue, path) =>
                  path.some(option => 
                    (option.label as string).toLowerCase().includes(inputValue.toLowerCase())
                  ),
              }}
              changeOnSelect
              maxTagCount="responsive"
            />
            <RangePicker
              placeholder={['寄件开始日期', '寄件结束日期']}
              value={sentTimeRange}
              onChange={(dates) => setSentTimeRange(dates)}
              style={{ width: isMobile ? '100%' : 'auto' }}
            />
            <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
              搜索
            </Button>
            <Button onClick={handleResetSearch}>
              重置
            </Button>
          </Space>
        </div>

        {data.length === 0 && !loading ? (
          <Empty
            description="暂无数据，请导入Excel文件"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          >
            <Button type="primary" icon={<UploadOutlined />} onClick={() => setModalVisible(true)}>
              导入数据
            </Button>
          </Empty>
        ) : (
          <Table
            columns={generateColumns()}
            dataSource={data}
            rowKey="id"
            loading={loading}
            pagination={pagination}
            onChange={handleTableChange}
            scroll={{ x: 1200 }}
            size="small"
          />
        )}
      </Card>

      <ImportModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleImportSuccess}
      />
    </div>
  )
}

export default Express
