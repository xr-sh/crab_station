import React, { useState, useEffect } from 'react'
import {
  Table,
  Button,
  Space,
  Card,
  Typography,
  message,
  Popconfirm,
  Select,
  Tag,
  Statistic,
  Row,
  Col,
  Empty,
} from 'antd'
import {
  UploadOutlined,
  DeleteOutlined,
  ClearOutlined,
  FileExcelOutlined,
  ReloadOutlined,
} from '@ant-design/icons'
import { expressApi, ExpressAnalysis, Statistics, EXPRESS_CATEGORIES } from '../../api/express'
import ImportModal from './components/ImportModal'

const { Title } = Typography

const Express: React.FC = () => {
  const [data, setData] = useState<ExpressAnalysis[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [columns, setColumns] = useState<string[]>([])
  const [selectedCategory, setSelectedCategory] = useState<string | undefined>('顺丰')
  const [modalVisible, setModalVisible] = useState(false)
  const [statistics, setStatistics] = useState<Statistics>({
    totalRecords: 0,
    fileCount: 0,
    columnCount: 0,
    categoryStats: {},
  })

  // 获取统计信息
  const fetchStatistics = async () => {
    try {
      const res: any = await expressApi.getStatistics()
      setStatistics(res)
    } catch (error: any) {
      console.error('获取统计失败', error)
    }
  }

  // 获取列名
  const fetchColumns = async () => {
    try {
      const res: any = await expressApi.getColumns()
      setColumns(res)
    } catch (error: any) {
      console.error('获取列名失败', error)
    }
  }

  // 获取数据列表
  const fetchData = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await expressApi.getList({
        page,
        size,
        sortBy: 'importedAt',
        sortDirection: 'DESC',
        category: selectedCategory,
      }) as any
      
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
    fetchColumns()
    fetchData()
  }, [])

  useEffect(() => {
    fetchData(0, pagination.pageSize)
  }, [selectedCategory])

  // 刷新数据
  const handleRefresh = () => {
    fetchStatistics()
    fetchColumns()
    fetchData(pagination.current - 1, pagination.pageSize)
  }

  // 删除单条记录
  const handleDelete = async (id: string) => {
    try {
      await expressApi.delete(id)
      message.success('删除成功')
      handleRefresh()
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
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

  // 导入成功回调
  const handleImportSuccess = () => {
    handleRefresh()
  }

  // 表格变化
  const handleTableChange = (newPagination: any) => {
    fetchData(newPagination.current - 1, newPagination.pageSize)
  }

  // 动态生成表格列
  const generateColumns = () => {
    // 固定列
    const fixedColumns = [
      {
        title: '类别',
        dataIndex: 'category',
        key: 'category',
        width: 100,
        render: (text: string) => (
          <Tag color={text === '顺丰' ? 'orange' : text === '京东' ? 'red' : 'blue'}>
            {text || '-'}
          </Tag>
        ),
      },
      {
        title: '文件名',
        dataIndex: 'fileName',
        key: 'fileName',
        width: 150,
        ellipsis: true,
        render: (text: string) => (
          <Tag icon={<FileExcelOutlined />} color="blue">
            {text}
          </Tag>
        ),
      },
      {
        title: 'Sheet',
        dataIndex: 'sheetName',
        key: 'sheetName',
        width: 100,
      },
      {
        title: '行号',
        dataIndex: 'rowNum',
        key: 'rowNum',
        width: 80,
      },
    ]

    // 动态列（从数据中提取）
    const dynamicColumns = columns.slice(0, 8).map(col => ({
      title: col,
      key: col,
      width: 150,
      ellipsis: true,
      render: (_: any, record: ExpressAnalysis) => {
        const value = record.dynamicFields?.[col]
        return value !== undefined && value !== null ? String(value) : '-'
      },
    }))

    // 操作列
    const actionColumn = {
      title: '操作',
      key: 'action',
      width: 100,
      fixed: 'right' as const,
      render: (_: any, record: ExpressAnalysis) => (
        <Popconfirm
          title="确认删除"
          description="确定要删除这条记录吗？"
          onConfirm={() => handleDelete(record.id)}
          okText="确定"
          cancelText="取消"
        >
          <Button type="link" danger icon={<DeleteOutlined />} size="small">
            删除
          </Button>
        </Popconfirm>
      ),
    }

    return [...fixedColumns, ...dynamicColumns, actionColumn]
  }

  return (
    <div style={{ padding: 24 }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="总记录数"
              value={statistics.totalRecords}
              suffix="条"
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="顺丰"
              value={statistics.categoryStats?.['顺丰'] || 0}
              suffix="条"
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="京东"
              value={statistics.categoryStats?.['京东'] || 0}
              suffix="条"
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Space style={{ display: 'flex', justifyContent: 'space-between' }}>
            <Title level={4} style={{ margin: 0 }}>快递分析</Title>
            <Space>
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
          </Space>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space>
            <span>筛选类别：</span>
            <Select
              placeholder="选择快递类别"
              allowClear
              style={{ width: 150 }}
              value={selectedCategory}
              onChange={setSelectedCategory}
              options={EXPRESS_CATEGORIES.map(name => ({ value: name, label: name }))}
            />
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