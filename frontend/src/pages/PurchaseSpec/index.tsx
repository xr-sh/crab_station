import React, { useEffect, useState } from 'react'
import {
  Button,
  Card,
  Drawer,
  Grid,
  Input,
  message,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd'
import {
  DeleteOutlined,
  EditOutlined,
  HistoryOutlined,
  PlusOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import { purchaseSpecApi, PurchaseSpec, PurchaseSpecPriceHistory } from '../../api/purchaseSpec'
import { withTablePagination } from '../../utils/tablePagination'
import PurchaseSpecModal from './components/PurchaseSpecModal'

const { Title } = Typography
const { useBreakpoint } = Grid

const formatPrice = (price: number | null | undefined) => {
  return price != null ? `${Number(price).toFixed(2)}` : '-'
}

const PurchaseSpecPage: React.FC = () => {
  const [specs, setSpecs] = useState<PurchaseSpec[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [filters, setFilters] = useState({
    name: '',
    status: undefined as number | undefined,
  })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingSpec, setEditingSpec] = useState<PurchaseSpec | null>(null)
  const [historyVisible, setHistoryVisible] = useState(false)
  const [historyLoading, setHistoryLoading] = useState(false)
  const [historySpec, setHistorySpec] = useState<PurchaseSpec | null>(null)
  const [priceHistory, setPriceHistory] = useState<PurchaseSpecPriceHistory[]>([])
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const fetchSpecs = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await purchaseSpecApi.getPurchaseSpecs({
        page,
        size,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
        name: filters.name || undefined,
        status: filters.status,
      }) as any
      setSpecs(res.content)
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.totalElements,
      })
    } catch (error: any) {
      message.error(error.message || '获取进货规格列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchSpecs()
  }, [])

  const handleTableChange = (newPagination: any) => {
    fetchSpecs(newPagination.current - 1, newPagination.pageSize)
  }

  const handleSearch = () => {
    fetchSpecs(0, pagination.pageSize)
  }

  const handleReset = () => {
    setFilters({
      name: '',
      status: undefined,
    })
    setTimeout(() => fetchSpecs(), 0)
  }

  const handleDelete = async (id: string) => {
    try {
      await purchaseSpecApi.deletePurchaseSpec(id)
      message.success('删除成功')
      fetchSpecs(pagination.current - 1, pagination.pageSize)
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  const handleAdd = () => {
    setEditingSpec(null)
    setModalVisible(true)
  }

  const handleEdit = (record: PurchaseSpec) => {
    setEditingSpec(record)
    setModalVisible(true)
  }

  const handleViewPriceHistory = async (record: PurchaseSpec) => {
    setHistorySpec(record)
    setHistoryVisible(true)
    setHistoryLoading(true)
    try {
      const res = await purchaseSpecApi.getPriceHistory(record.id) as any
      setPriceHistory(res)
    } catch (error: any) {
      message.error(error.message || '获取价格历史失败')
    } finally {
      setHistoryLoading(false)
    }
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchSpecs(pagination.current - 1, pagination.pageSize)
  }

  const columns = [
    {
      title: '规格范围(两)',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '价格(元/斤)',
      dataIndex: 'price',
      key: 'price',
      render: formatPrice,
    },
    {
      title: '类别',
      dataIndex: 'category',
      key: 'category',
      render: (category: string | null) => category || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
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
      render: (date: string) => date ? new Date(date).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      render: (_: any, record: PurchaseSpec) => (
        <Space size="small" wrap>
          <Button
            type="primary"
            icon={<EditOutlined />}
            size="small"
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            icon={<HistoryOutlined />}
            size="small"
            onClick={() => handleViewPriceHistory(record)}
          >
            价格历史
          </Button>
          <Popconfirm
            title="确认删除"
            description="确定要删除这条进货规格吗？"
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

  const historyColumns = [
    {
      title: '原价格',
      dataIndex: 'oldPrice',
      key: 'oldPrice',
      render: formatPrice,
    },
    {
      title: '新价格',
      dataIndex: 'newPrice',
      key: 'newPrice',
      render: formatPrice,
    },
    {
      title: '变动时间',
      dataIndex: 'changedAt',
      key: 'changedAt',
      render: (date: string) => date ? new Date(date).toLocaleString() : '-',
    },
  ]

  return (
    <div style={{ padding: isMobile ? 16 : 24 }}>
      <Card>
        <div style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', flexDirection: isMobile ? 'column' : 'row', justifyContent: 'space-between', gap: isMobile ? 12 : 0 }}>
            <Title level={4} style={{ margin: 0 }}>进货规格管理</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增规格
            </Button>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Input
              placeholder="规格范围"
              value={filters.name}
              onChange={(e) => setFilters(prev => ({ ...prev, name: e.target.value }))}
              onPressEnter={handleSearch}
              style={{ width: isMobile ? '100%' : 200 }}
            />
            <Select
              placeholder="状态"
              allowClear
              style={{ width: isMobile ? '100%' : 120 }}
              value={filters.status}
              onChange={(value) => setFilters(prev => ({ ...prev, status: value }))}
              options={[
                { value: 1, label: '启用' },
                { value: 0, label: '禁用' },
              ]}
            />
            <Space wrap>
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                搜索
              </Button>
              <Button onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={specs}
          rowKey="id"
          loading={loading}
          pagination={withTablePagination(pagination)}
          onChange={handleTableChange}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <PurchaseSpecModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        spec={editingSpec}
      />

      <Drawer
        title={historySpec ? `价格历史 - ${historySpec.name}` : '价格历史'}
        open={historyVisible}
        onClose={() => setHistoryVisible(false)}
        width={isMobile ? '100%' : 720}
      >
        <Table
          columns={historyColumns}
          dataSource={priceHistory}
          rowKey="id"
          loading={historyLoading}
          pagination={false}
          size="small"
          scroll={{ x: 'max-content' }}
        />
      </Drawer>
    </div>
  )
}

export default PurchaseSpecPage
