import React, { useEffect, useState } from 'react'
import { Modal, Form, Input, Switch, message, Button, Space, Typography } from 'antd'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons'
import { apiConfigApi, ApiConfig as ApiConfigType, CreateApiConfigRequest, UpdateApiConfigRequest } from '../../../api/apiConfig'

const { Text } = Typography

interface ApiConfigModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  config: ApiConfigType | null
}

interface DynamicField {
  key: string
  value: string
}

const ApiConfigModal: React.FC<ApiConfigModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  config,
}) => {
  const [form] = Form.useForm()
  const [dynamicFields, setDynamicFields] = useState<DynamicField[]>([])
  const isEditing = !!config

  useEffect(() => {
    if (visible) {
      if (config) {
        form.setFieldsValue({
          platformName: config.platformName,
          apiKey: config.apiKey,
          secret: config.secret,
          baseUrl: config.baseUrl,
          status: config.status === 1,
          remark: config.remark,
        })
        // 解析动态配置
        if (config.dynamicConfig) {
          const fields = Object.entries(config.dynamicConfig).map(([key, value]) => ({
            key,
            value: String(value),
          }))
          setDynamicFields(fields)
        } else {
          setDynamicFields([])
        }
      } else {
        form.resetFields()
        form.setFieldsValue({ status: true })
        setDynamicFields([])
      }
    }
  }, [visible, config, form])

  const handleAddDynamicField = () => {
    setDynamicFields([...dynamicFields, { key: '', value: '' }])
  }

  const handleRemoveDynamicField = (index: number) => {
    setDynamicFields(dynamicFields.filter((_, i) => i !== index))
  }

  const handleDynamicFieldChange = (index: number, field: 'key' | 'value', value: string) => {
    const newFields = [...dynamicFields]
    newFields[index][field] = value
    setDynamicFields(newFields)
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()

      // 构建动态配置对象
      const dynamicConfig: Record<string, any> = {}
      dynamicFields.forEach(field => {
        if (field.key && field.value) {
          dynamicConfig[field.key] = field.value
        }
      })

      const submitData = {
        ...values,
        status: values.status ? 1 : 0,
        dynamicConfig: dynamicFields.length > 0 ? dynamicConfig : undefined,
      }

      if (isEditing) {
        await apiConfigApi.updateApiConfig(config!.id, submitData as UpdateApiConfigRequest)
        message.success('API配置更新成功')
      } else {
        await apiConfigApi.createApiConfig(submitData as CreateApiConfigRequest)
        message.success('API配置创建成功')
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
      title={isEditing ? '编辑API配置' : '新增API配置'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      okText={isEditing ? '更新' : '创建'}
      cancelText="取消"
      width={600}
    >
      <Form
        form={form}
        layout="vertical"
        autoComplete="off"
        style={{ marginTop: 16 }}
      >
        <Form.Item
          label="平台名称"
          name="platformName"
          rules={[
            { required: true, message: '请输入平台名称' },
            { max: 100, message: '平台名称最多100个字符' },
          ]}
        >
          <Input placeholder="如：京东、淘宝、拼多多" />
        </Form.Item>

        <Form.Item
          label="API Key"
          name="apiKey"
          rules={[
            { required: true, message: '请输入API Key' },
            { max: 200, message: 'API Key最多200个字符' },
          ]}
        >
          <Input placeholder="如：appKey、apiKey、clientId" />
        </Form.Item>

        <Form.Item
          label="Secret / Token"
          name="secret"
          rules={[
            { max: 200, message: 'Secret最多200个字符' },
          ]}
        >
          <Input placeholder="如：appSecret、accessToken" />
        </Form.Item>

        <Form.Item
          label="API Base URL"
          name="baseUrl"
          rules={[
            { max: 500, message: 'Base URL最多500个字符' },
          ]}
        >
          <Input placeholder="如：https://api.jd.com" />
        </Form.Item>

        <Form.Item label="动态配置（可选）">
          <Text type="secondary" style={{ marginBottom: 8, display: 'block' }}>
            用于存储平台特定的配置项，如京东的 accessToken、refreshToken 等
          </Text>
          <Space direction="vertical" style={{ width: '100%' }}>
            {dynamicFields.map((field, index) => (
              <Space key={index} style={{ width: '100%' }}>
                <Input
                  placeholder="配置项名称"
                  value={field.key}
                  onChange={(e) => handleDynamicFieldChange(index, 'key', e.target.value)}
                  style={{ width: 150 }}
                />
                <Input
                  placeholder="配置项值"
                  value={field.value}
                  onChange={(e) => handleDynamicFieldChange(index, 'value', e.target.value)}
                  style={{ width: 250 }}
                />
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => handleRemoveDynamicField(index)}
                />
              </Space>
            ))}
            <Button
              type="dashed"
              icon={<PlusOutlined />}
              onClick={handleAddDynamicField}
              style={{ width: '100%' }}
            >
              添加配置项
            </Button>
          </Space>
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

export default ApiConfigModal