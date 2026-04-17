import React, { useState, useEffect } from 'react'
import {
  Table,
  Button,
  Space,
  Tag,
  Popconfirm,
  message,
  Card,
  Typography,
  DatePicker,
  Select,
  Statistic,
  Row,
  Col,
  Grid,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  DollarOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { financeApi, FinanceRecord } from '../../api/finance'
import FinanceModal from './components/FinanceModal'

const { Title } = Typography
const { RangePicker } = DatePicker
const { useBreakpoint } = Grid

const Finance: React.FC = () => {
  const [records, setRecords] = useState<FinanceRecord[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [filters, setFilters] = useState({
    type: undefined as string | undefined,
    startDate: undefined as string | undefined,
    endDate: undefined as string | undefined,
  })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRecord, setEditingRecord] = useState<FinanceRecord | null>(null)
  const [statistics, setStatistics] = useState({
    income: 0,
    expense: 0,
    balance: 0,
  })
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const fetchRecords = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await financeApi.getFinanceRecords({
        page,
        size,
        sortBy: 'recordDate',
        sortDirection: 'DESC',
        type: filters.type,
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
      message.error(error.message || '获取财务记录失败')
    } finally {
      setLoading(false)
    }
  }

  const calculateStatistics = (data: FinanceRecord[]) => {
    const income = data
      .filter(r => r.type === '收入')
      .reduce((sum, r) => sum + r.amount, 0)
    const expense = data
      .filter(r => r.type === '支出')
      .reduce((sum, r) => sum + r.amount, 0)
    setStatistics({
      income,
      expense,
      balance: income - expense,
    })
  }

  useEffect(() => {
    fetchRecords()
  }, [filters])

  const handleTableChange = (newPagination: any) => {
    fetchRecords(newPagination.current - 1, newPagination.pageSize)
  }

  const handleTypeChange = (value: string | undefined) => {
    setFilters(prev => ({ ...prev, type: value }))
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
      await financeApi.deleteFinanceRecord(id)
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

  const handleEdit = (record: FinanceRecord) => {
    setEditingRecord(record)
    setModalVisible(true)
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchRecords(pagination.current - 1, pagination.pageSize)
  }

  const columns = [
    {
      title: '日期',
      dataIndex: 'recordDate',
      key: 'recordDate',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
    },
    {
      title: '类别',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => (
        <Tag color={type === '收入' ? 'green' : 'red'}>
          {type === '收入' ? <ArrowUpOutlined /> : <ArrowDownOutlined />} {type}
        </Tag>
      ),
    },
    {
      title: '金额',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount: number, record: FinanceRecord) => (
        <span style={{ color: record.type === '收入' ? '#52c41a' : '#ff4d4f', fontWeight: 'bold' }}>
          {record.type === '收入' ? '+' : '-'}{amount.toFixed(2)}
        </span>
      ),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      render: (remark: string) => remark || '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: FinanceRecord) => (
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
            description={`确定要删除这条${record.type}记录吗？`}
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
    <div style={{ padding: isMobile ? 16 : 24 }}>
      {/* 统计卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} md={8}>
          <Card>
            <Statistic
              title="总收入"
              value={statistics.income}
              precision={2}
              valueStyle={{ color: '#52c41a' }}
              prefix={<ArrowUpOutlined />}
              suffix="元"
            />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Statistic
              title="总支出"
              value={statistics.expense}
              precision={2}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<ArrowDownOutlined />}
              suffix="元"
            />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card>
            <Statistic
              title="结余"
              value={statistics.balance}
              precision={2}
              valueStyle={{ color: statistics.balance >= 0 ? '#52c41a' : '#ff4d4f' }}
              prefix={<DollarOutlined />}
              suffix="元"
            />
          </Card>
        </Col>
      </Row>

      <Card>
        <div style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', flexDirection: isMobile ? 'column' : 'row', justifyContent: 'space-between', gap: isMobile ? 12 : 0 }}>
            <Title level={4} style={{ margin: 0 }}>财务管理</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增记录
            </Button>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Select
              placeholder="选择类别"
              allowClear
              style={{ width: isMobile ? '100%' : 120 }}
              value={filters.type}
              onChange={handleTypeChange}
              options={[
                { value: '收入', label: '收入' },
                { value: '支出', label: '支出' },
              ]}
            />
            <RangePicker
              placeholder={['开始日期', '结束日期']}
              onChange={handleDateChange}
              style={{ width: isMobile ? '100%' : 'auto' }}
            />
            <Button onClick={() => {
              setFilters({ type: undefined, startDate: undefined, endDate: undefined })
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
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <FinanceModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        record={editingRecord}
      />
    </div>
  )
}

export default Finance
