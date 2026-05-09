import React, { useEffect } from 'react'
import { Modal, Form, Input, InputNumber, Select, Switch, message } from 'antd'
import { purchaseSpecApi, PurchaseSpec, CreatePurchaseSpecRequest, UpdatePurchaseSpecRequest } from '../../../api/purchaseSpec'

interface PurchaseSpecModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  spec: PurchaseSpec | null
}

const PurchaseSpecModal: React.FC<PurchaseSpecModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  spec,
}) => {
  const [form] = Form.useForm()
  const isEditing = !!spec

  useEffect(() => {
    if (visible) {
      if (spec) {
        form.setFieldsValue({
          name: spec.name,
          category: spec.category,
          price: spec.price,
          priceChangeReason: undefined,
          status: spec.status === 1,
          remark: spec.remark,
        })
      } else {
        form.resetFields()
        form.setFieldsValue({ status: true })
      }
    }
  }, [visible, spec, form])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const submitData = {
        ...values,
        status: values.status ? 1 : 0,
      }

      if (isEditing) {
        await purchaseSpecApi.updatePurchaseSpec(spec!.id, submitData as UpdatePurchaseSpecRequest)
        message.success('进货规格更新成功')
      } else {
        await purchaseSpecApi.createPurchaseSpec(submitData as CreatePurchaseSpecRequest)
        message.success('进货规格创建成功')
      }

      onSuccess()
    } catch (error: any) {
      if (error.errorFields) {
        return
      }
      message.error(error.message || (isEditing ? '更新失败' : '创建失败'))
    }
  }

  return (
    <Modal
      title={isEditing ? '编辑进货规格' : '新增进货规格'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      okText={isEditing ? '更新' : '创建'}
      cancelText="取消"
      width={500}
    >
      <Form
        form={form}
        layout="vertical"
        autoComplete="off"
        style={{ marginTop: 16 }}
      >
        <Form.Item
          label="规格范围(两)"
          name="name"
          rules={[
            { required: true, message: '请输入规格范围' },
            { max: 100, message: '规格范围最多100个字符' },
          ]}
        >
          <Input placeholder="如：3-5" />
        </Form.Item>

        <Form.Item
          label="类别"
          name="category"
          rules={[{ required: true, message: '请选择类别' }]}
        >
          <Select
            placeholder="请选择类别"
            options={[
              { value: '公', label: '公' },
              { value: '母', label: '母' },
            ]}
          />
        </Form.Item>

        <Form.Item
          label="价格(元/斤)"
          name="price"
        >
          <InputNumber
            min={0}
            precision={2}
            style={{ width: '100%' }}
            placeholder="请输入价格"
          />
        </Form.Item>

        <Form.Item
          label="状态"
          name="status"
          valuePropName="checked"
        >
          <Switch checkedChildren="启用" unCheckedChildren="禁用" defaultChecked />
        </Form.Item>

        <Form.Item
          label="备注"
          name="remark"
        >
          <Input.TextArea
            rows={3}
            placeholder="请输入备注（选填）"
            maxLength={500}
            showCount
          />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default PurchaseSpecModal
