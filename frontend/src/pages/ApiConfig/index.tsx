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
import { apiConfigApi, ApiConfig as ApiConfigType } from '../../api/apiConfig'
import ApiConfigModal from './components/ApiConfigModal'

const { Title } = Typography
const { useBreakpoint } = Grid

const ApiConfigPage: React.FC = () => {
  const [configs, setConfigs] = useState<ApiConfigType[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [filters, setFilters] = useState({
    platformName: '',
    status: undefined as number | undefined,
  })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingConfig, setEditingConfig] = useState<ApiConfigType | null>(null)
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const fetchConfigs = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await apiConfigApi.getApiConfigs({
        page,
        size,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
        platformName: filters.platformName || undefined,
        status: filters.status,
      }) as any
      setConfigs(res.content)
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.totalElements,
      })
    } catch (error: any) {
      message.error(error.message || '获取API配置列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchConfigs()
  }, [])

  const handleTableChange = (newPagination: any) => {
    fetchConfigs(newPagination.current - 1, newPagination.pageSize)
  }

  const handleSearch = () => {
    fetchConfigs(0, pagination.pageSize)
  }

  const handleReset = () => {
    setFilters({
      platformName: '',
      status: undefined,
    })
    setTimeout(() => fetchConfigs(), 0)
  }

  const handleDelete = async (id: string) => {
    try {
      await apiConfigApi.deleteApiConfig(id)
      message.success('删除成功')
      fetchConfigs(pagination.current - 1, pagination.pageSize)
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  const handleAdd = () => {
    setEditingConfig(null)
    setModalVisible(true)
  }

  const handleEdit = (record: ApiConfigType) => {
    setEditingConfig(record)
    setModalVisible(true)
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchConfigs(pagination.current - 1, pagination.pageSize)
  }

  const columns = [
    {
      title: '平台名称',
      dataIndex: 'platformName',
      key: 'platformName',
    },
    {
      title: 'API Key',
      dataIndex: 'apiKey',
      key: 'apiKey',
      render: (key: string) => key ? `${key.substring(0, 8)}...` : '-',
    },
    {
      title: 'Secret',
      dataIndex: 'secret',
      key: 'secret',
      render: (secret: string) => secret ? `${secret.substring(0, 8)}...` : '-',
    },
    {
      title: 'Base URL',
      dataIndex: 'baseUrl',
      key: 'baseUrl',
      render: (url: string) => url || '-',
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
      render: (_: any, record: ApiConfigType) => (
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
            description="确定要删除这条API配置吗？"
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
            <Title level={4} style={{ margin: 0 }}>API配置管理</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增配置
            </Button>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Input
              placeholder="平台名称"
              value={filters.platformName}
              onChange={(e) => setFilters(prev => ({ ...prev, platformName: e.target.value }))}
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
          dataSource={configs}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <ApiConfigModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        config={editingConfig}
      />
    </div>
  )
}

export default ApiConfigPage