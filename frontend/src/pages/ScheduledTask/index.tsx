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
  Tooltip,
} from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  SyncOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons'
import { scheduledTaskApi, ScheduledTask as ScheduledTaskType } from '../../api/scheduledTask'
import ScheduledTaskModal from './components/ScheduledTaskModal'

const { Title } = Typography
const { useBreakpoint } = Grid

const taskTypeOptions = [
  { value: 'DATA_SYNC', label: '数据同步' },
  { value: 'DATA_CLEAN', label: '数据清理' },
  { value: 'REPORT_GEN', label: '报表生成' },
  { value: 'API_CALL', label: 'API调用' },
  { value: 'OTHER', label: '其他' },
]

const executeStatusColors: Record<string, string> = {
  SUCCESS: 'green',
  FAILED: 'red',
  RUNNING: 'blue',
  PENDING: 'default',
}

const ScheduledTaskPage: React.FC = () => {
  const [tasks, setTasks] = useState<ScheduledTaskType[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [filters, setFilters] = useState({
    taskName: '',
    taskType: undefined as string | undefined,
    status: undefined as number | undefined,
  })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingTask, setEditingTask] = useState<ScheduledTaskType | null>(null)
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const fetchTasks = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await scheduledTaskApi.getScheduledTasks({
        page,
        size,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
        taskName: filters.taskName || undefined,
        taskType: filters.taskType,
        status: filters.status,
      }) as any
      setTasks(res.content)
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.totalElements,
      })
    } catch (error: any) {
      message.error(error.message || '获取定时任务列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchTasks()
  }, [])

  const handleTableChange = (newPagination: any) => {
    fetchTasks(newPagination.current - 1, newPagination.pageSize)
  }

  const handleSearch = () => {
    fetchTasks(0, pagination.pageSize)
  }

  const handleReset = () => {
    setFilters({
      taskName: '',
      taskType: undefined,
      status: undefined,
    })
    setTimeout(() => fetchTasks(), 0)
  }

  const handleDelete = async (id: string) => {
    try {
      await scheduledTaskApi.deleteScheduledTask(id)
      message.success('删除成功')
      fetchTasks(pagination.current - 1, pagination.pageSize)
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  const handleToggleStatus = async (id: string) => {
    try {
      await scheduledTaskApi.toggleStatus(id)
      message.success('状态切换成功')
      fetchTasks(pagination.current - 1, pagination.pageSize)
    } catch (error: any) {
      message.error(error.message || '状态切换失败')
    }
  }

  const handleAdd = () => {
    setEditingTask(null)
    setModalVisible(true)
  }

  const handleEdit = (record: ScheduledTaskType) => {
    setEditingTask(record)
    setModalVisible(true)
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchTasks(pagination.current - 1, pagination.pageSize)
  }

  const getTaskTypeLabel = (type: string) => {
    const option = taskTypeOptions.find(o => o.value === type)
    return option ? option.label : type
  }

  const columns = [
    {
      title: '任务名称',
      dataIndex: 'taskName',
      key: 'taskName',
    },
    {
      title: '任务类型',
      dataIndex: 'taskType',
      key: 'taskType',
      render: (type: string) => getTaskTypeLabel(type),
    },
    {
      title: 'Cron表达式',
      dataIndex: 'cronExpression',
      key: 'cronExpression',
      render: (cron: string) => (
        <Tooltip title={cron}>
          <span><ClockCircleOutlined /> {cron}</span>
        </Tooltip>
      ),
    },
    {
      title: '执行次数',
      dataIndex: 'executeCount',
      key: 'executeCount',
      render: (count: number) => count || 0,
    },
    {
      title: '上次执行状态',
      dataIndex: 'lastExecuteStatus',
      key: 'lastExecuteStatus',
      render: (status: string) => {
        if (!status) return '-'
        const color = executeStatusColors[status] || 'default'
        const labels: Record<string, string> = {
          SUCCESS: '成功',
          FAILED: '失败',
          RUNNING: '运行中',
          PENDING: '待执行',
        }
        return <Tag color={color}>{labels[status] || status}</Tag>
      },
    },
    {
      title: '上次执行时间',
      dataIndex: 'lastExecuteTime',
      key: 'lastExecuteTime',
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
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
      width: 250,
      render: (_: any, record: ScheduledTaskType) => (
        <Space size="small">
          <Button
            type="primary"
            icon={<EditOutlined />}
            size="small"
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            icon={<SyncOutlined />}
            size="small"
            onClick={() => handleToggleStatus(record.id)}
          >
            {record.status === 1 ? '禁用' : '启用'}
          </Button>
          <Popconfirm
            title="确认删除"
            description="确定要删除这个定时任务吗？"
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
            <Title level={4} style={{ margin: 0 }}>定时任务管理</Title>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增任务
            </Button>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Input
              placeholder="任务名称"
              value={filters.taskName}
              onChange={(e) => setFilters(prev => ({ ...prev, taskName: e.target.value }))}
              onPressEnter={handleSearch}
              style={{ width: isMobile ? '100%' : 200 }}
            />
            <Select
              placeholder="任务类型"
              allowClear
              style={{ width: isMobile ? '100%' : 150 }}
              value={filters.taskType}
              onChange={(value) => setFilters(prev => ({ ...prev, taskType: value }))}
              options={taskTypeOptions}
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
          dataSource={tasks}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <ScheduledTaskModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        task={editingTask}
      />
    </div>
  )
}

export default ScheduledTaskPage