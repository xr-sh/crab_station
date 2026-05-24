import React, { useEffect, useState } from 'react'
import {
  Button,
  Card,
  Grid,
  Input,
  InputNumber,
  message,
  Modal,
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
  SettingOutlined,
} from '@ant-design/icons'
import { platformPackageApi, PlatformPackage } from '../../api/platformPackage'
import PlatformPackageModal from './components/PlatformPackageModal'

const { Title } = Typography
const { useBreakpoint } = Grid

const formatPrice = (price: number | null | undefined) => {
  if (price === null || price === undefined) {
    return '-'
  }
  return price.toFixed(2)
}

const calculatePurchaseCost = (record: PlatformPackage) => {
  const total = (record.priceRules || []).reduce((sum, rule) => {
    return sum + (rule.costPrice || 0)
  }, 0)
  return total > 0 ? total : null
}

const calculateTotalCost = (record: PlatformPackage) => {
  if (record.totalCost !== null && record.totalCost !== undefined) {
    return record.totalCost
  }
  const purchaseCost = calculatePurchaseCost(record)
  return purchaseCost === null ? null : purchaseCost + (record.fixedCost || 0)
}

const calculateProfit = (record: PlatformPackage) => {
  const totalCost = calculateTotalCost(record)
  if (record.price === null || record.price === undefined || totalCost === null) {
    return null
  }
  return record.price - totalCost
}

const PlatformPackagePage: React.FC = () => {
  const [packages, setPackages] = useState<PlatformPackage[]>([])
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
  const [costConfigVisible, setCostConfigVisible] = useState(false)
  const [fixedCost, setFixedCost] = useState<number | null>(null)
  const [fixedCostRemark, setFixedCostRemark] = useState('')
  const [editingPackage, setEditingPackage] = useState<PlatformPackage | null>(null)
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const fetchPackages = async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await platformPackageApi.getPlatformPackages({
        page,
        size,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
        name: filters.name || undefined,
        status: filters.status,
      }) as any
      setPackages(res.content)
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.totalElements,
      })
    } catch (error: any) {
      message.error(error.message || '获取平台套餐失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPackages()
  }, [])

  const handleTableChange = (newPagination: any) => {
    fetchPackages(newPagination.current - 1, newPagination.pageSize)
  }

  const handleSearch = () => {
    fetchPackages(0, pagination.pageSize)
  }

  const handleReset = () => {
    setFilters({
      name: '',
      status: undefined,
    })
    setTimeout(() => fetchPackages(), 0)
  }

  const handleDelete = async (id: string) => {
    try {
      await platformPackageApi.deletePlatformPackage(id)
      message.success('删除成功')
      fetchPackages(pagination.current - 1, pagination.pageSize)
    } catch (error: any) {
      message.error(error.message || '删除失败')
    }
  }

  const handleAdd = () => {
    setEditingPackage(null)
    setModalVisible(true)
  }

  const handleEdit = async (record: PlatformPackage) => {
    try {
      const detail = await platformPackageApi.getPlatformPackageById(record.id) as any
      setEditingPackage(detail)
      setModalVisible(true)
    } catch (error: any) {
      message.error(error.message || '获取平台套餐详情失败')
    }
  }

  const handleModalSuccess = () => {
    setModalVisible(false)
    fetchPackages(pagination.current - 1, pagination.pageSize)
  }

  const handleOpenCostConfig = async () => {
    try {
      const res = await platformPackageApi.getCostConfig() as any
      setFixedCost(res.fixedCost ?? 0)
      setFixedCostRemark(res.remark || '')
      setCostConfigVisible(true)
    } catch (error: any) {
      message.error(error.message || '获取固定成本配置失败')
    }
  }

  const handleSaveCostConfig = async () => {
    try {
      await platformPackageApi.updateCostConfig({
        fixedCost: fixedCost ?? 0,
        remark: fixedCostRemark,
      })
      message.success('固定成本配置已保存')
      setCostConfigVisible(false)
      fetchPackages(pagination.current - 1, pagination.pageSize)
    } catch (error: any) {
      message.error(error.message || '保存固定成本配置失败')
    }
  }

  const columns = [
    {
      title: '套餐名称',
      dataIndex: 'name',
      key: 'name',
      minWidth: 240,
    },
    {
      title: '价格',
      dataIndex: 'price',
      key: 'price',
      width: 120,
      render: (price: number | null) => formatPrice(price),
    },
    {
      title: '进货成本',
      key: 'purchaseCost',
      width: 120,
      render: (_: any, record: PlatformPackage) => formatPrice(calculatePurchaseCost(record)),
    },
    {
      title: '总成本',
      key: 'totalCost',
      width: 120,
      render: (_: any, record: PlatformPackage) => formatPrice(calculateTotalCost(record)),
    },
    {
      title: '利润',
      key: 'profit',
      width: 120,
      render: (_: any, record: PlatformPackage) => {
        const profit = calculateProfit(record)
        if (profit === null) {
          return '-'
        }
        return (
          <Tag color={profit >= 0 ? 'green' : 'red'}>
            {formatPrice(profit)}
          </Tag>
        )
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
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
      width: 180,
      render: (date: string) => date ? new Date(date).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: isMobile ? undefined : 'right' as const,
      render: (_: any, record: PlatformPackage) => (
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
            title="确认删除平台套餐？"
            description="删除后不可恢复，请确认是否继续。"
            onConfirm={() => handleDelete(record.id)}
            okText="确认"
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
          <div style={{ display: 'flex', flexDirection: isMobile ? 'column' : 'row', justifyContent: 'space-between', gap: 12 }}>
            <Title level={4} style={{ margin: 0 }}>平台套餐</Title>
            <Space wrap>
              <Button icon={<SettingOutlined />} onClick={handleOpenCostConfig}>
                固定成本
              </Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                新增套餐
              </Button>
            </Space>
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Space wrap direction={isMobile ? 'vertical' : 'horizontal'} style={{ width: isMobile ? '100%' : 'auto' }}>
            <Input
              placeholder="套餐名称"
              value={filters.name}
              onChange={(e) => setFilters(prev => ({ ...prev, name: e.target.value }))}
              onPressEnter={handleSearch}
              style={{ width: isMobile ? '100%' : 220 }}
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
          dataSource={packages}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <PlatformPackageModal
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
        platformPackage={editingPackage}
      />

      <Modal
        title="固定成本配置"
        open={costConfigVisible}
        onOk={handleSaveCostConfig}
        onCancel={() => setCostConfigVisible(false)}
        okText="保存"
        cancelText="取消"
      >
        <Space direction="vertical" style={{ width: '100%' }} size={16}>
          <InputNumber
            min={0}
            precision={2}
            value={fixedCost}
            onChange={(value) => setFixedCost(value)}
            style={{ width: '100%' }}
            addonAfter="元/套餐"
            placeholder="请输入人工、包装等固定成本"
          />
          <Input.TextArea
            rows={3}
            maxLength={500}
            showCount
            value={fixedCostRemark}
            onChange={(event) => setFixedCostRemark(event.target.value)}
            placeholder="请输入固定成本备注，例如人工、包装、耗材等说明"
          />
        </Space>
      </Modal>
    </div>
  )
}

export default PlatformPackagePage
