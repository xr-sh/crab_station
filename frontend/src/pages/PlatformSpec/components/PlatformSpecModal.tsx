import React, { useEffect } from 'react'
import { Modal, Form, Input, Select, Switch, message } from 'antd'
import { platformSpecApi, PlatformSpec, CreatePlatformSpecRequest, UpdatePlatformSpecRequest } from '../../../api/platformSpec'

interface PlatformSpecModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  spec: PlatformSpec | null
}

const PlatformSpecModal: React.FC<PlatformSpecModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  spec,
}) => {
  const [form] = Form.useForm()
  const isEditing = !!spec
  const validateSpecRange = (_: unknown, value?: string) => {
    if (!value) {
      return Promise.resolve()
    }
    const trimmedValue = value.trim()
    if (!/^\d+(\.\d+)?-\d+(\.\d+)?$/.test(trimmedValue)) {
      return Promise.reject(new Error('规格范围格式必须为数字-数字，例如2.3-2.6'))
    }
    const [min, max] = trimmedValue.split('-').map(Number)
    if (min >= max) {
      return Promise.reject(new Error('规格范围左侧数值必须小于右侧数值'))
    }
    return Promise.resolve()
  }

  useEffect(() => {
    if (visible) {
      if (spec) {
        form.setFieldsValue({
          name: spec.name,
          category: spec.category,
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
        await platformSpecApi.updatePlatformSpec(spec!.id, submitData as UpdatePlatformSpecRequest)
        message.success('平台规格更新成功')
      } else {
        await platformSpecApi.createPlatformSpec(submitData as CreatePlatformSpecRequest)
        message.success('平台规格创建成功')
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
      title={isEditing ? '编辑平台规格' : '新增平台规格'}
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
            { validator: validateSpecRange },
          ]}
        >
          <Input placeholder="例如：2.3-2.6" />
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

export default PlatformSpecModal
