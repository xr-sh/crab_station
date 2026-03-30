import React, { useState, useEffect } from 'react'
import {
  Table,
  Button,
  Space,
  Popconfirm,
  message,
  Card,
  Typography,
  DatePicker,
  Input,
  Statistic,
  Row,
  Col,
  Tag,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  ContainerOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { purchaseApi, PurchaseRecord } from '../../api/purchase'
import PurchaseModal from './components/PurchaseModal'

const { Title } = Typography
const { RangePicker } = DatePicker

const Purchase: React.FC = () => {
  const [records, setRecords] = useState<PurchaseRecord[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [filters, setFilters] = useState({
    supplier: '',
    startDate: undefined as string | undefined,
    endDate: undefined as string | undefined,
  })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRecord, setEditingRecord] = useState<PurchaseRecord | null>(null)
  const [statistics, setStatistics] = useState({
    totalCount: 0,
    totalWeight: 0,
    totalAmount: 0,
  })

  const fetchRecords = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await purchaseApi.getPurchaseRecords({
        page,
        size,
        sortBy: 'purchaseDate',
        sortDirection: 'DESC',
        supplier: filters.supplier || undefined,
        startDate: filters.startDate,
        endDate: filters.endDate,
      }) as any
      setRecords(res.content)
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.totalElements,
      })
      
      // 计算统计
      calculateStatistics(res.content)
    } catch (error: any) {
      message.error(error.message || '获取进货记录失败')
    } finally {
      setLoading(false)
    }
  }

  const calculateStatistics = (data: PurchaseRecord[]) => {
    const totalCount = data.length
    const totalWeight = data.reduce((sum, r) => sum + (r.totalWeight || 0), 0)
    const totalAmount = data.reduce((sum, r) => sum + (r.totalAmount || 0), 0)
    setStatistics({
      totalCount,
      totalWeight,
      totalAmount,
    })
  }

  useEffect(() => {
    fetchRecords()
  }, [filters])

  const handleTableChange = (newPagination: any) => {
    fetchRecords(newPagination.current - 1, newPagination.pageSize)
  }

  const handleSupplierChange = (value: string) => {
    setFilters(prev => ({ ...prev, supplier: value }))
  }

  const handleDateChange = (dates: any) => {
    if (dates) {
      setFilters(prev => ({
        ...prev,
        startDate: dates[0]?.format('YYYY-MM-DD'),
        endDate: dates[1]?.format('YYYY-MM-DD'),
      }))
    } else {
      setFilters(prev => ({
        ...prev,
        startDate: undefined,
        endDate: undefined,
      }))
    }
  }

  const handleDelete = async (id: string) => {
    try {
      await purchaseApi.deletePurchaseRecord(id)
      message.success('删除成功')
      fetchRecords(pagination.current - 1, pagination.pageSize)
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  const handleAdd = () => {
    setEditingRecord(null)
    setModalVisible(true)
  }

  const handleEdit = (record: PurchaseRecord) => {
    setEditingRecord(record)
    setModalVisible(true)
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchRecords(pagination.current - 1, pagination.pageSize)
  }

  const expandedRowRender = (record: PurchaseRecord) => {
    const itemColumns = [
      {
        title: '规格',
        dataIndex: 'purchaseSpecName',
        key: 'purchaseSpecName',
      },
      {
        title: '重量(kg)',
        dataIndex: 'weight',
        key: 'weight',
        render: (weight: number) => weight?.toFixed(2),
      },
      {
        title: '单价(元/kg)',
        dataIndex: 'unitPrice',
        key: 'unitPrice',
        render: (price: number) => price?.toFixed(2),
      },
      {
        title: '金额(元)',
        dataIndex: 'amount',
        key: 'amount',
        render: (amount: number) => (
          <span style={{ color: '#1890ff', fontWeight: 'bold' }}>
            {amount?.toFixed(2)}
          </span>
        ),
      },
    ]

    return (
      <Table
        columns={itemColumns}
        dataSource={record.items}
        pagination={false}
        rowKey="id"
        size="small"
        bordered
        summary={() => (
          <Table.Summary fixed>
            <Table.Summary.Row>
              <Table.Summary.Cell index={0}><strong>合计</strong></Table.Summary.Cell>
              <Table.Summary.Cell index={1}>
                <strong>{record.totalWeight?.toFixed(2)} kg</strong>
              </Table.Summary.Cell>
              <Table.Summary.Cell index={2}>-</Table.Summary.Cell>
              <Table.Summary.Cell index={3}>
                <strong style={{ color: '#1890ff' }}>{record.totalAmount?.toFixed(2)} 元</strong>
              </Table.Summary.Cell>
            </Table.Summary.Row>
          </Table.Summary>
        )}
      />
    )
  }

  const columns = [
    {
      title: '进货日期',
      dataIndex: 'purchaseDate',
      key: 'purchaseDate',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
    {
      title: '供应商',
      dataIndex: 'supplier',
      key: 'supplier',
      render: (supplier: string) => supplier || '-',
    },
    {
      title: '规格数',
      key: 'itemCount',
      render: (_: any, record: PurchaseRecord) => (
        <Tag color="blue">{record.items?.length || 0} 种规格</Tag>
      ),
    },
    {
      title: '总重量(kg)',
      dataIndex: 'totalWeight',
      key: 'totalWeight',
      render: (weight: number) => (
        <span style={{ color: '#52c41a' }}>{weight?.toFixed(2)}</span>
      ),
    },
    {
      title: '总金额(元)',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (amount: number) => (
        <span style={{ color: '#1890ff', fontWeight: 'bold' }}>{amount?.toFixed(2)}</span>
      ),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      render: (remark: string) => remark || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: PurchaseRecord) => (
        <Space size="middle">
          <Button
            type="primary"
            icon={<EditOutlined />}
            size="small"
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除"
            description="确定要删除这条进货记录吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="primary"
              danger
              icon={<DeleteOutlined />}
              size="small"
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="进货单数"
              value={statistics.totalCount}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="总重量"
              value={statistics.totalWeight}
              precision={2}
              prefix={<ContainerOutlined />}
              suffix="kg"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="总金额"
              value={statistics.totalAmount}
              precision={2}
              prefix={<DollarOutlined />}
              suffix="元"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
      </Row>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Space style={{ display: 'flex', justifyContent: 'space-between' }}>
            <Title level={4} style={{ margin: 0 }}>进货管理</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增进货
            </Button>
          </Space>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space>
            <Input
              placeholder="搜索供应商"
              style={{ width: 200 }}
              value={filters.supplier}
              onChange={(e) => handleSupplierChange(e.target.value)}
              onPressEnter={() => fetchRecords(0, pagination.pageSize)}
            />
            <RangePicker
              placeholder={['开始日期', '结束日期']}
              onChange={handleDateChange}
            />
            <Button onClick={() => {
              setFilters({ supplier: '', startDate: undefined, endDate: undefined })
            }}>
              重置筛选
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={records}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
          expandable={{
            expandedRowRender,
            rowExpandable: (record) => record.items && record.items.length > 0,
          }}
        />
      </Card>

      <PurchaseModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        record={editingRecord}
      />
    </div>
  )
}

export default Purchase
