import { getAntdCascaderData } from 'cn-division'

/**
 * 中国省市区三级联动数据
 * 用于 antd Cascader 组件
 * 数据来源：民政部 2025年最新行政区划数据
 */
export interface AreaOption {
  value: string
  label: string
  children?: AreaOption[]
}

/**
 * 获取省市区级联数据（不带编码）
 * 适用于 antd Cascader 组件
 */
export const getAreaOptions = (): AreaOption[] => {
  return getAntdCascaderData() as AreaOption[]
}

/**
 * 省市区数据（缓存）
 */
let cachedAreaOptions: AreaOption[] | null = null

/**
 * 获取缓存的省市区数据
 */
export const getAreaOptionsCached = (): AreaOption[] => {
  if (!cachedAreaOptions) {
    cachedAreaOptions = getAreaOptions()
  }
  return cachedAreaOptions
}

/**
 * 根据选中的省市区数组获取完整地址字符串
 * @param selectedAreas 选中的省市区数组，如 ['广东省', '深圳市', '南山区']
 * @returns 完整地址字符串，如 '广东省深圳市南山区'
 */
export const getFullAddress = (selectedAreas: string[] | undefined): string => {
  if (!selectedAreas || selectedAreas.length === 0) {
    return ''
  }
  return selectedAreas.join('')
}