import React, { useEffect } from 'react'
import { Modal, Form, Input, Switch, message } from 'antd'
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

  useEffect(() => {
    if (visible) {
      if (spec) {
        form.setFieldsValue({
          name: spec.name,
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
          label="规格名称"
          name="name"
          rules={[
            { required: true, message: '请输入规格名称' },
            { max: 100, message: '规格名称最多100个字符' },
          ]}
        >
          <Input placeholder="如：标准装" />
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