import React, { useEffect, useState } from 'react'
import {
  Button,
  Card,
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
  PlusOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import { specificationMappingApi, SpecificationMapping } from '../../api/specificationMapping'
import MappingModal from './components/MappingModal'

const { Title } = Typography
const { useBreakpoint } = Grid

const SpecificationMappingPage: React.FC = () => {
  const [mappings, setMappings] = useState<SpecificationMapping[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [filters, setFilters] = useState({
    purchaseSpecName: '',
    platformSpecName: '',
    status: undefined as number | undefined,
  })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingMapping, setEditingMapping] = useState<SpecificationMapping | null>(null)
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const fetchMappings = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await specificationMappingApi.getSpecificationMappings({
        page,
        size,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
        purchaseSpecName: filters.purchaseSpecName || undefined,
        platformSpecName: filters.platformSpecName || undefined,
        status: filters.status,
      }) as any
      setMappings(res.content)
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.totalElements,
      })
    } catch (error: any) {
      message.error(error.message || '获取规格映射列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchMappings()
  }, [])

  const handleTableChange = (newPagination: any) => {
    fetchMappings(newPagination.current - 1, newPagination.pageSize)
  }

  const handleSearch = () => {
    fetchMappings(0, pagination.pageSize)
  }

  const handleReset = () => {
    setFilters({
      purchaseSpecName: '',
      platformSpecName: '',
      status: undefined,
    })
    setTimeout(() => fetchMappings(), 0)
  }

  const handleDelete = async (id: string) => {
    try {
      await specificationMappingApi.deleteSpecificationMapping(id)
      message.success('删除成功')
      fetchMappings(pagination.current - 1, pagination.pageSize)
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  const handleAdd = () => {
    setEditingMapping(null)
    setModalVisible(true)
  }

  const handleEdit = (record: SpecificationMapping) => {
    setEditingMapping(record)
    setModalVisible(true)
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchMappings(pagination.current - 1, pagination.pageSize)
  }

  const columns = [
    {
      title: '类别',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) => category || '-',
    },
    {
      title: '进货规格',
      dataIndex: 'purchaseSpecName',
      key: 'purchaseSpecName',
    },
    {
      title: '平台规格',
      dataIndex: 'platformSpecName',
      key: 'platformSpecName',
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
      width: 200,
      render: (_: any, record: SpecificationMapping) => (
        <Space size="small" wrap>
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
            description="确定要删除这条规格映射吗？"
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
      <Card>
        <div style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', flexDirection: isMobile ? 'column' : 'row', justifyContent: 'space-between', gap: isMobile ? 12 : 0 }}>
            <Title level={4} style={{ margin: 0 }}>规格映射</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增映射
            </Button>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Input
              placeholder="进货规格"
              value={filters.purchaseSpecName}
              onChange={(e) => setFilters(prev => ({ ...prev, purchaseSpecName: e.target.value }))}
              onPressEnter={handleSearch}
              style={{ width: isMobile ? '100%' : 150 }}
            />
            <Input
              placeholder="平台规格"
              value={filters.platformSpecName}
              onChange={(e) => setFilters(prev => ({ ...prev, platformSpecName: e.target.value }))}
              onPressEnter={handleSearch}
              style={{ width: isMobile ? '100%' : 150 }}
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
          dataSource={mappings}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <MappingModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        mapping={editingMapping}
      />
    </div>
  )
}

export default SpecificationMappingPage
