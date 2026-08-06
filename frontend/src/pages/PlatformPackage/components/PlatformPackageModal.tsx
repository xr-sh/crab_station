import React, { useEffect, useMemo, useState } from 'react'
import { Button, Cascader, Form, Input, InputNumber, message, Modal, Space, Switch, Table, Typography } from 'antd'
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons'
import { platformPackageApi, CreatePlatformPackageRequest, PlatformPackage, PlatformSpec, UpdatePlatformPackageRequest } from '../../../api/platformPackage'

interface PlatformPackageModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  platformPackage: PlatformPackage | null
}

type SelectionRow = {
  key: string
  path?: Array<string | number>
}

type CascaderOption = {
  value: string | number
  label: string
  children?: CascaderOption[]
}

const qtyOptions = [6, 8, 10, 12]

const formatPrice = (price: number | null | undefined) => {
  if (price === null || price === undefined) {
    return '-'
  }
  return `¥${price.toFixed(2)}`
}

const PlatformPackageModal: React.FC<PlatformPackageModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  platformPackage,
}) => {
  const [form] = Form.useForm()
  const [platformSpecs, setPlatformSpecs] = useState<PlatformSpec[]>([])
  const selections = Form.useWatch('selections', form) as SelectionRow[] | undefined
  const isEditing = !!platformPackage

  const specMap = useMemo(() => {
    return new Map(platformSpecs.map(spec => [spec.id, spec]))
  }, [platformSpecs])

  const cascaderOptions = useMemo<CascaderOption[]>(() => {
    const categoryMap = new Map<string, PlatformSpec[]>()
    platformSpecs.forEach(spec => {
      const category = spec.category || ''
      if (!categoryMap.has(category)) {
        categoryMap.set(category, [])
      }
      categoryMap.get(category)!.push(spec)
    })

    return Array.from(categoryMap.entries()).map(([category, specs]) => ({
      value: category,
      label: category,
      children: specs.map(spec => ({
        value: spec.id,
        label: spec.name,
        children: qtyOptions.map(qty => ({
          value: qty,
          label: `${qty}只`,
        })),
      })),
    }))
  }, [platformSpecs])

  const fetchSpecs = async () => {
    try {
      const res = await platformPackageApi.getActivePlatformSpecs() as any
      setPlatformSpecs(res)
    } catch (error: any) {
      message.error(error.message || '获取平台规格失败')
    }
  }

  const buildName = (rows?: SelectionRow[]) => {
    const parts = (rows || [])
      .map(row => row.path)
      .filter((path): path is Array<string | number> => !!path && path.length === 3)
      .map(path => {
        const category = String(path[0] || '')
        const specId = String(path[1] || '')
        const qty = String(path[2] || '')
        const spec = specMap.get(specId)
        if (!category || !spec || !qty) {
          return ''
        }
        return `${spec.name}${category}${qty}只`
      })
      .filter(Boolean)

    return parts.join(', ')
  }

  useEffect(() => {
    if (visible) {
      fetchSpecs()
    }
  }, [visible])

  useEffect(() => {
    if (!visible) {
      return
    }

    if (platformPackage) {
      const selectionRows: SelectionRow[] = (platformPackage.items || []).map((item, index) => {
        const priceRule = platformPackage.priceRules?.[index]
        return {
          key: item.id || `${index}`,
          path: [
            item.platformSpecCategory || '',
            item.platformSpecId,
            priceRule?.qty ?? 6,
          ],
        }
      })

      form.setFieldsValue({
        selections: selectionRows.length > 0 ? selectionRows : [{ key: '0', path: undefined }],
        name: platformPackage.name,
        price: platformPackage.price,
        status: platformPackage.status === 1,
        remark: platformPackage.remark,
      })
    } else {
      form.resetFields()
      form.setFieldsValue({
        status: true,
        selections: [{ key: '0', path: undefined }],
      })
    }
  }, [visible, platformPackage, form])

  useEffect(() => {
    const name = buildName(selections)
    if (name || !isEditing) {
      form.setFieldValue('name', name)
    }
  }, [selections, specMap, form, isEditing])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const selectionRows: SelectionRow[] = values.selections || []
      const parsedRows = selectionRows
        .map(row => row.path)
        .filter((path): path is Array<string | number> => !!path && path.length === 3)

      if (parsedRows.length === 0) {
        message.error('请至少选择一个规格组合')
        return
      }

      const submitData = {
        selections: parsedRows.map(path => ({
          category: String(path[0]),
          platformSpecId: String(path[1]),
          qty: Number(path[2]),
        })),
        price: values.price,
        status: values.status ? 1 : 0,
        remark: values.remark,
      }

      if (isEditing) {
        await platformPackageApi.updatePlatformPackage(platformPackage!.id, submitData as UpdatePlatformPackageRequest)
        message.success('平台套餐更新成功')
      } else {
        await platformPackageApi.createPlatformPackage(submitData as CreatePlatformPackageRequest)
        message.success('平台套餐创建成功')
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
      title={isEditing ? '编辑平台套餐' : '新增平台套餐'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      okText={isEditing ? '更新' : '创建'}
      cancelText="取消"
      width={960}
    >
      <Form
        form={form}
        layout="vertical"
        autoComplete="off"
        style={{ marginTop: 16 }}
        initialValues={{ status: true, selections: [] }}
      >
        <Form.Item label="状态" name="status" valuePropName="checked">
          <Switch checkedChildren="启用" unCheckedChildren="禁用" />
        </Form.Item>

        <Form.Item label="备注" name="remark">
          <Input.TextArea rows={3} maxLength={500} showCount />
        </Form.Item>

        <Form.List name="selections">
          {(fields, { add, remove }) => (
            <>
              <Space style={{ marginBottom: 8, display: 'flex', justifyContent: 'space-between' }}>
                <Typography.Text strong>规格选择</Typography.Text>
                <Button
                  icon={<PlusOutlined />}
                  onClick={() => add({ key: `${fields.length}`, path: undefined })}
                  disabled={fields.length >= 2}
                >
                  添加规格
                </Button>
              </Space>
              <Table
                dataSource={fields.map(field => ({ ...field, key: field.key }))}
                pagination={false}
                size="small"
                rowKey="key"
                columns={[
                  {
                    title: '类别 / 规格 / 只数',
                    render: (_: any, field: any) => (
                      <Form.Item name={[field.name, 'path']} rules={[{ required: true, message: '请选择类别、规格和只数' }]} style={{ marginBottom: 0 }}>
                        <Cascader
                          options={cascaderOptions}
                          placeholder="请选择类别、规格和只数"
                          style={{ width: '100%' }}
                          changeOnSelect={false}
                        />
                      </Form.Item>
                    ),
                  },
                  {
                    title: '成本',
                    width: 140,
                    render: (_: any, field: any) => {
                      const rule = platformPackage?.priceRules?.[field.name]
                      return formatPrice(rule?.costPrice)
                    },
                  },
                  {
                    title: '操作',
                    width: 80,
                    render: (_: any, field: any) => (
                      <Button
                        danger
                        icon={<DeleteOutlined />}
                        onClick={() => remove(field.name)}
                        disabled={fields.length <= 1}
                      />
                    ),
                  },
                ]}
              />
            </>
          )}
        </Form.List>

        <Form.Item
          label="套餐名称"
          name="name"
          rules={[{ required: true, message: '套餐名称会根据所选规格自动生成' }]}
          style={{ marginTop: 16 }}
        >
          <Input disabled placeholder="将根据所选规格自动生成" />
        </Form.Item>

        <Form.Item
          label="价格"
          name="price"
          rules={[{ required: true, message: '请输入价格' }]}
        >
          <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入价格" addonBefore="¥" />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default PlatformPackageModal
