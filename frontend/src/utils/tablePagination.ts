export const TABLE_PAGE_SIZE_OPTIONS = ['10', '20', '50', '100']

export const withTablePagination = <T extends object>(pagination: T) => ({
  ...pagination,
  showSizeChanger: true,
  pageSizeOptions: TABLE_PAGE_SIZE_OPTIONS,
  showTotal: (total: number) => `共 ${total} 条`,
})
