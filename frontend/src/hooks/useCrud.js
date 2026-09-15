import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

/**
 * Shared list+search+pagination+CRUD-mutations plumbing for a master-data
 * resource. Every master data page (products, categories, customers, ...)
 * uses this instead of re-implementing the same react-query wiring.
 */
export function useCrud(resourceApi, queryKey) {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const size = 10

  const listQuery = useQuery({
    queryKey: [queryKey, 'list', search, page],
    queryFn: () => resourceApi.list({ search, page, size, sort: 'createdAt,desc' }),
    placeholderData: (prev) => prev,
  })

  function invalidate() {
    queryClient.invalidateQueries({ queryKey: [queryKey, 'list'] })
  }

  const createMutation = useMutation({
    mutationFn: (payload) => resourceApi.create(payload),
    onSuccess: invalidate,
  })
  const updateMutation = useMutation({
    mutationFn: ({ id, payload }) => resourceApi.update(id, payload),
    onSuccess: invalidate,
  })
  const removeMutation = useMutation({
    mutationFn: (id) => resourceApi.remove(id),
    onSuccess: invalidate,
  })

  return {
    data: listQuery.data,
    isLoading: listQuery.isLoading,
    isFetching: listQuery.isFetching,
    error: listQuery.error,
    search,
    setSearch: (value) => {
      setPage(0)
      setSearch(value)
    },
    page,
    setPage,
    create: createMutation.mutateAsync,
    update: updateMutation.mutateAsync,
    remove: removeMutation.mutateAsync,
    isSaving: createMutation.isPending || updateMutation.isPending,
  }
}
