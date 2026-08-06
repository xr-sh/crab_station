import React, { useEffect } from 'react'
import {
  Modal,
  Form,
  Input,
  DatePicker,
  InputNumber,
  Button,
  Space,
  Table,
  Card,
  Typography,
  message,
  Select,
} from 'antd'
import {
  PlusOutlined,
  DeleteOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { purchaseApi, PurchaseRecord, CreatePurchaseRecordRequest, UpdatePurchaseRecordRequest } from '../../../api/purchase'
import { purchaseSpecApi, PurchaseSpec } from '../../../api/purchaseSpec'

const { TextArea } = Input
const { Text } = Typography

interface PurchaseModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  record: PurchaseRecord | null
}

interface PurchaseItemForm {
  id?: string
  purchaseSpecId: string
  purchaseSpecName?: string
  weight: number
  unitPrice: number
  amount: number
}

const PurchaseModal: React.FC<PurchaseModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  record,
}) => {
  const [form] = Form.useForm()
  const [items, setItems] = React.useState<PurchaseItemForm[]>([])
  const [purchaseSpecs, setPurchaseSpecs] = React.useState<PurchaseSpec[]>([])
  const [loadingSpecs, setLoadingSpecs] = React.useState(false)
  const isEditing = !!record

  // 加载进货规格列表
  const fetchPurchaseSpecs = async () => {
    setLoadingSpecs(true)
    try {
      const res = await purchaseSpecApi.getAllPurchaseSpecs() as any
      // 只获取启用状态的规格
      const activeSpecs = res.filter((spec: PurchaseSpec) => spec.status === 1)
      setPurchaseSpecs(activeSpecs)
    } catch (error: any) {
      message.error(error.message || '获取进货规格列表失败')
    } finally {
      setLoadingSpecs(false)
    }
  }

  useEffect(() => {
    if (visible) {
      fetchPurchaseSpecs()
      if (record) {
        form.setFieldsValue({
          purchaseDate: dayjs(record.purchaseDate),
          supplier: record.supplier,
          remark: record.remark,
        })
        // 设置明细
        const formItems = record.items?.map(item => ({
          id: item.id,
          purchaseSpecId: item.purchaseSpecId,
          purchaseSpecName: item.purchaseSpecName,
          weight: item.weight,
          unitPrice: item.unitPrice,
          amount: item.amount,
        })) || []
        setItems(formItems)
      } else {
        form.resetFields()
        form.setFieldsValue({
          purchaseDate: dayjs(),
        })
        setItems([])
      }
    }
  }, [visible, record, form])

  const calculateAmount = (weight: number, unitPrice: number) => {
    return Number((weight * unitPrice).toFixed(2))
  }

  const calculateTotal = () => {
    return items.reduce((sum, item) => sum + (item.amount || 0), 0)
  }

  const calculateTotalWeight = () => {
    return items.reduce((sum, item) => sum + (item.weight || 0), 0)
  }

  const handleAddItem = () => {
    setItems([...items, { purchaseSpecId: '', weight: 0, unitPrice: 0, amount: 0 }])
  }

  const handleDeleteItem = (index: number) => {
    const newItems = items.filter((_, i) => i !== index)
    setItems(newItems)
  }

  const handleItemChange = (index: number, field: keyof PurchaseItemForm, value: any) => {
    const newItems = [...items]
    newItems[index] = { ...newItems[index], [field]: value }
    
    // 如果修改了规格，更新规格名称
    if (field === 'purchaseSpecId') {
      const spec = purchaseSpecs.find(s => s.id === value)
      newItems[index].purchaseSpecName = spec?.name || ''
    }
    
    // 如果修改了重量或单价，自动计算金额
    if (field === 'weight' || field === 'unitPrice') {
      const weight = field === 'weight' ? value : newItems[index].weight
      const unitPrice = field === 'unitPrice' ? value : newItems[index].unitPrice
      newItems[index].amount = calculateAmount(weight, unitPrice)
    }
    
    setItems(newItems)
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      // 验证至少有一个明细
      if (items.length === 0) {
        message.error('请至少添加一个进货明细')
        return
      }

      // 验证明细完整性
      for (let i = 0; i < items.length; i++) {
        const item = items[i]
        if (!item.purchaseSpecId || !item.weight || !item.unitPrice) {
          message.error(`第 ${i + 1} 行明细信息不完整`)
          return
        }
      }

      const formItems = items.map(item => ({
        id: item.id,
        purchaseSpecId: item.purchaseSpecId,
        weight: item.weight,
        unitPrice: item.unitPrice,
      }))

      const data = {
        ...values,
        purchaseDate: values.purchaseDate.format('YYYY-MM-DD'),
        items: formItems,
      }

      if (isEditing) {
        await purchaseApi.updatePurchaseRecord(record.id, data as UpdatePurchaseRecordRequest)
        message.success('更新成功')
      } else {
        await purchaseApi.createPurchaseRecord(data as CreatePurchaseRecordRequest)
        message.success('创建成功')
      }
      
      onSuccess()
    } catch (error: any) {
      message.error(error.message || (isEditing ? '更新失败' : '创建失败'))
    }
  }

  const itemColumns = [
    {
      title: '规格',
      dataIndex: 'purchaseSpecId',
      key: 'purchaseSpecId',
      width: 200,
      render: (_: any, __: any, index: number) => (
        <Select
          style={{ width: '100%' }}
          placeholder="选择规格"
          value={items[index]?.purchaseSpecId}
          onChange={(value) => handleItemChange(index, 'purchaseSpecId', value)}
          loading={loadingSpecs}
          options={purchaseSpecs.map(spec => ({
            value: spec.id,
            label: spec.name,
          }))}
          showSearch
          filterOption={(input, option) =>
            (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
          }
        />
      ),
    },
    {
      title: '重量(斤)',
      dataIndex: 'weight',
      key: 'weight',
      width: 150,
      render: (_: any, __: any, index: number) => (
        <InputNumber
          style={{ width: '100%' }}
          min={0.01}
          precision={2}
          placeholder="重量"
          value={items[index]?.weight}
          onChange={(value) => handleItemChange(index, 'weight', value)}
        />
      ),
    },
    {
      title: '单价(¥/斤)',
      dataIndex: 'unitPrice',
      key: 'unitPrice',
      width: 150,
      render: (_: any, __: any, index: number) => (
        <InputNumber
          style={{ width: '100%' }}
          min={0.01}
          precision={2}
          placeholder="单价"
          addonBefore="¥"
          value={items[index]?.unitPrice}
          onChange={(value) => handleItemChange(index, 'unitPrice', value)}
        />
      ),
    },
    {
      title: '金额(¥)',
      dataIndex: 'amount',
      key: 'amount',
      width: 120,
      render: (_: any, __: any, index: number) => (
        <Text strong style={{ color: '#1890ff' }}>
          ¥{items[index]?.amount?.toFixed(2) || '0.00'}
        </Text>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      render: (_: any, __: any, index: number) => (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          onClick={() => handleDeleteItem(index)}
        />
      ),
    },
  ]

  return (
    <Modal
      title={isEditing ? '编辑进货记录' : '新增进货记录'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      okText="保存"
      cancelText="取消"
      width={900}
      bodyStyle={{ maxHeight: '70vh', overflow: 'auto' }}
    >
      <Form
        form={form}
        layout="vertical"
        autoComplete="off"
      >
        <Space style={{ width: '100%' }} size="large">
          <Form.Item
            name="purchaseDate"
            label="进货日期"
            rules={[{ required: true, message: '请选择进货日期' }]}
            style={{ width: 200 }}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="supplier"
            label="供应商"
            style={{ width: 250 }}
          >
            <Input placeholder="请输入供应商" />
          </Form.Item>
        </Space>

        <Form.Item
          name="remark"
          label="备注"
        >
          <TextArea
            rows={2}
            placeholder="请输入备注（选填）"
            maxLength={500}
            showCount
          />
        </Form.Item>
      </Form>

      <Card
        title="进货明细"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAddItem}>
            添加规格
          </Button>
        }
        style={{ marginTop: 16 }}
      >
        <Table
          columns={itemColumns}
          dataSource={items.map((item, index) => ({ ...item, key: index }))}
          pagination={false}
          size="small"
          bordered
          locale={{ emptyText: '暂无明细，请点击"添加规格"' }}
          summary={() => (
            <Table.Summary fixed>
              <Table.Summary.Row>
                <Table.Summary.Cell index={0}><strong>合计</strong></Table.Summary.Cell>
                <Table.Summary.Cell index={1}>
                  <strong style={{ color: '#52c41a' }}>{calculateTotalWeight().toFixed(2)} 斤</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={2}>-</Table.Summary.Cell>
                <Table.Summary.Cell index={3}>
                  <strong style={{ color: '#1890ff' }}>¥{calculateTotal().toFixed(2)}</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={4}>-</Table.Summary.Cell>
              </Table.Summary.Row>
            </Table.Summary>
          )}
        />
      </Card>
    </Modal>
  )
}

export default PurchaseModal
