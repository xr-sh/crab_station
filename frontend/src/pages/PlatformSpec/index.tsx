import React, { useState, useEffect } from 'react'
import {
  Table,
  Button,
  Input,
  Space,
  Popconfirm,
  message,
  Card,
  Typography,
  Select,
  Tag,
  Grid,
} from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons'
import { platformSpecApi, PlatformSpec } from '../../api/platformSpec'
import PlatformSpecModal from './components/PlatformSpecModal'

const { Title } = Typography
const { useBreakpoint } = Grid

const PlatformSpecPage: React.FC = () => {
  const [specs, setSpecs] = useState<PlatformSpec[]>([])
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
  const [editingSpec, setEditingSpec] = useState<PlatformSpec | null>(null)
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const fetchSpecs = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await platformSpecApi.getPlatformSpecs({
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
      message.error(error.message || '获取平台规格列表失败')
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
      await platformSpecApi.deletePlatformSpec(id)
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

  const handleEdit = (record: PlatformSpec) => {
    setEditingSpec(record)
    setModalVisible(true)
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchSpecs(pagination.current - 1, pagination.pageSize)
  }

  const columns = [
    {
      title: '规格名称',
      dataIndex: 'name',
      key: 'name',
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
      render: (_: any, record: PlatformSpec) => (
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
            description="确定要删除这条平台规格吗？"
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
            <Title level={4} style={{ margin: 0 }}>平台规格管理</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增规格
            </Button>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Input
              placeholder="规格名称"
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
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <PlatformSpecModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        spec={editingSpec}
      />
    </div>
  )
}

export default PlatformSpecPage