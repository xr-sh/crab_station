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
  Switch,
  Tooltip,
  Grid,
} from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons'
import { userApi, User } from '../../api/users'
import { withTablePagination } from '../../utils/tablePagination'
import UserModal from './components/UserModal'

const { Title } = Typography
const { useBreakpoint } = Grid

const Users: React.FC = () => {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [keyword, setKeyword] = useState('')
  const [modalVisible, setModalVisible] = useState(false)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const screens = useBreakpoint()

  // 判断是否为移动端
  const isMobile = !screens.md

  const fetchUsers = async (page = 0, size = 10, searchKeyword = '') => {
    setLoading(true)
    try {
      const res = await userApi.getUsers({
        page,
        size,
        keyword: searchKeyword,
        sortBy: 'id',
        sortDirection: 'DESC',
      }) as any
      setUsers(res.content)
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.totalElements,
      })
    } catch (error: any) {
      message.error(error.message || '获取用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchUsers()
  }, [])

  const handleTableChange = (newPagination: any) => {
    fetchUsers(newPagination.current - 1, newPagination.pageSize, keyword)
  }

  const handleSearch = () => {
    fetchUsers(0, pagination.pageSize, keyword)
  }

  const handleDelete = async (id: string) => {
    try {
      await userApi.deleteUser(id)
      message.success('删除成功')
      fetchUsers(pagination.current - 1, pagination.pageSize, keyword)
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  const handleStatusChange = async (id: string, checked: boolean) => {
    try {
      await userApi.updateUserStatus(id, checked ? 1 : 0)
      message.success('状态更新成功')
      fetchUsers(pagination.current - 1, pagination.pageSize, keyword)
    } catch (error: any) {
      message.error(error.message || '状态更新失败')
    }
  }

  const handleAdd = () => {
    setEditingUser(null)
    setModalVisible(true)
  }

  const handleEdit = (user: User) => {
    setEditingUser(user)
    setModalVisible(true)
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchUsers(pagination.current - 1, pagination.pageSize, keyword)
  }

  const columns = [
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      render: (email: string) => email || '-',
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      render: (phone: string) => phone || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number, record: User) => (
        <Tooltip title={status === 1 ? '点击禁用' : '点击启用'}>
          <Switch
            checked={status === 1}
            onChange={(checked) => handleStatusChange(record.id, checked)}
            checkedChildren="启用"
            unCheckedChildren="禁用"
          />
        </Tooltip>
      ),
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
      render: (_: any, record: User) => (
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
            description={`确定要删除用户 "${record.username}" 吗？`}
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
            <Title level={4} style={{ margin: 0 }}>用户管理</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              添加用户
            </Button>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Input
              placeholder="搜索用户名或邮箱"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onPressEnter={handleSearch}
              style={{ width: isMobile ? '100%' : 250 }}
              prefix={<SearchOutlined />}
            />
            <Space wrap>
              <Button type="primary" onClick={handleSearch}>
                搜索
              </Button>
              <Button onClick={() => {
                setKeyword('')
                fetchUsers(0, pagination.pageSize, '')
              }}>
                重置
              </Button>
            </Space>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          pagination={withTablePagination(pagination)}
          onChange={handleTableChange}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <UserModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        user={editingUser}
      />
    </div>
  )
}

export default Users
