import React, { useEffect, useMemo, useState } from 'react'
import { Modal, Form, Select, Switch, message, Input } from 'antd'
import { specificationMappingApi, SpecificationMapping, CreateSpecificationMappingRequest, UpdateSpecificationMappingRequest } from '../../../api/specificationMapping'
import { purchaseSpecApi, PurchaseSpec } from '../../../api/purchaseSpec'
import { platformSpecApi, PlatformSpec } from '../../../api/platformSpec'

interface MappingModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  mapping: SpecificationMapping | null
}

const categoryOptions = [
  { value: '公', label: '公' },
  { value: '母', label: '母' },
]

const MappingModal: React.FC<MappingModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  mapping,
}) => {
  const [form] = Form.useForm()
  const isEditing = !!mapping
  const [purchaseSpecs, setPurchaseSpecs] = useState<PurchaseSpec[]>([])
  const [platformSpecs, setPlatformSpecs] = useState<PlatformSpec[]>([])
  const [loading, setLoading] = useState(false)
  const category = Form.useWatch('category', form)

  const filteredPurchaseSpecs = useMemo(
    () => purchaseSpecs.filter(spec => spec.category === category),
    [purchaseSpecs, category],
  )

  const filteredPlatformSpecs = useMemo(
    () => platformSpecs.filter(spec => spec.category === category),
    [platformSpecs, category],
  )

  const fetchSpecs = async () => {
    setLoading(true)
    try {
      const [purchaseRes, platformRes] = await Promise.all([
        purchaseSpecApi.getAllPurchaseSpecs(),
        platformSpecApi.getAllPlatformSpecs(),
      ])
      setPurchaseSpecs(purchaseRes as any)
      setPlatformSpecs(platformRes as any)
    } catch (error: any) {
      message.error('获取规格列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (visible) {
      fetchSpecs()
      if (mapping) {
        form.setFieldsValue({
          category: mapping.category,
          purchaseSpecId: mapping.purchaseSpecId,
          platformSpecId: mapping.platformSpecId,
          status: mapping.status === 1,
          remark: mapping.remark,
        })
      } else {
        form.resetFields()
        form.setFieldsValue({ status: true })
      }
    }
  }, [visible, mapping, form])

  const handleCategoryChange = () => {
    form.setFieldsValue({
      purchaseSpecId: undefined,
      platformSpecId: undefined,
    })
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const submitData = {
        ...values,
        status: values.status ? 1 : 0,
      }

      if (isEditing) {
        await specificationMappingApi.updateSpecificationMapping(mapping!.id, submitData as UpdateSpecificationMappingRequest)
        message.success('规格映射更新成功')
      } else {
        await specificationMappingApi.createSpecificationMapping(submitData as CreateSpecificationMappingRequest)
        message.success('规格映射创建成功')
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
      title={isEditing ? '编辑规格映射' : '新增规格映射'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      okText={isEditing ? '更新' : '创建'}
      cancelText="取消"
      width={500}
      confirmLoading={loading}
    >
      <Form
        form={form}
        layout="vertical"
        autoComplete="off"
        style={{ marginTop: 16 }}
      >
        <Form.Item
          label="类别"
          name="category"
          rules={[{ required: true, message: '请选择类别' }]}
        >
          <Select
            placeholder="请选择类别"
            options={categoryOptions}
            onChange={handleCategoryChange}
          />
        </Form.Item>

        <Form.Item
          label="平台规格"
          name="platformSpecId"
          rules={[{ required: true, message: '请选择平台规格' }]}
        >
          <Select
            placeholder={category ? '请选择平台规格' : '请先选择类别'}
            showSearch
            disabled={!category}
            optionFilterProp="label"
            options={filteredPlatformSpecs.map(spec => ({
              value: spec.id,
              label: spec.name,
            }))}
          />
        </Form.Item>

        <Form.Item
          label="进货规格"
          name="purchaseSpecId"
          rules={[{ required: true, message: '请选择进货规格' }]}
        >
          <Select
            placeholder={category ? '请选择进货规格' : '请先选择类别'}
            showSearch
            disabled={!category}
            optionFilterProp="label"
            options={filteredPurchaseSpecs.map(spec => ({
              value: spec.id,
              label: spec.name,
            }))}
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

export default MappingModal
