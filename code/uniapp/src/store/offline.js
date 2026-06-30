import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import offlineDB from '@/utils/offlineDB.js'
import { request } from '@/api/index.js'

export const useOfflineStore = defineStore('offline', () => {
  const pendingList = ref([])
  const syncStatus = ref('idle')
  const syncProgress = ref(0)
  const syncResult = ref({ success: 0, failed: 0, total: 0 })

  const stats = computed(() => {
    const list = pendingList.value
    return {
      total: list.length,
      pending: list.filter(item => item.status === 'pending').length,
      syncing: list.filter(item => item.status === 'syncing').length,
      failed: list.filter(item => item.status === 'failed').length,
      success: list.filter(item => item.status === 'success').length
    }
  })

  function loadPending() {
    pendingList.value = offlineDB.getSyncQueue()
  }

  function addPending(item) {
    const result = offlineDB.addToSyncQueue(item)
    if (result) {
      pendingList.value.unshift(result)
    }
    return result
  }

  async function syncOne(syncId) {
    const item = pendingList.value.find(i => i.syncId === syncId)
    if (!item || item.status === 'syncing') return null

    item.status = 'syncing'
    offlineDB.updateSyncQueueItem(syncId, { status: 'syncing' })

    try {
      const result = await request({
        url: item.apiUrl || '/api/v1/offline/sync',
        method: 'POST',
        data: item.data
      })

      item.status = 'success'
      item.syncedAt = Date.now()
      offlineDB.updateSyncQueueItem(syncId, { status: 'success', syncedAt: Date.now() })

      setTimeout(() => {
        offlineDB.removeFromSyncQueue(syncId)
        const idx = pendingList.value.findIndex(i => i.syncId === syncId)
        if (idx > -1) {
          pendingList.value.splice(idx, 1)
        }
      }, 3000)

      return { success: true, data: result }
    } catch (err) {
      item.status = 'failed'
      item.retryCount = (item.retryCount || 0) + 1
      item.errorMsg = err?.message || '同步失败'
      offlineDB.updateSyncQueueItem(syncId, {
        status: 'failed',
        retryCount: item.retryCount,
        errorMsg: item.errorMsg
      })
      return { success: false, error: err }
    }
  }

  async function syncAll() {
    if (syncStatus.value === 'syncing') return

    const pendingItems = pendingList.value.filter(item => item.status !== 'success')
    if (pendingItems.length === 0) {
      return { success: 0, failed: 0, total: 0 }
    }

    syncStatus.value = 'syncing'
    syncProgress.value = 0
    syncResult.value = { success: 0, failed: 0, total: pendingItems.length }

    for (let i = 0; i < pendingItems.length; i++) {
      const item = pendingItems[i]
      const result = await syncOne(item.syncId)

      if (result && result.success) {
        syncResult.value.success++
      } else {
        syncResult.value.failed++
      }

      syncProgress.value = Math.round(((i + 1) / pendingItems.length) * 100)
    }

    syncStatus.value = 'idle'
    return syncResult.value
  }

  function removeItem(syncId) {
    offlineDB.removeFromSyncQueue(syncId)
    const idx = pendingList.value.findIndex(i => i.syncId === syncId)
    if (idx > -1) {
      pendingList.value.splice(idx, 1)
    }
  }

  function retryItem(syncId) {
    const item = pendingList.value.find(i => i.syncId === syncId)
    if (item) {
      item.status = 'pending'
      item.errorMsg = ''
      offlineDB.updateSyncQueueItem(syncId, { status: 'pending', errorMsg: '' })
    }
  }

  function clearFailed() {
    const failedIds = pendingList.value
      .filter(item => item.status === 'failed')
      .map(item => item.syncId)

    failedIds.forEach(id => {
      offlineDB.removeFromSyncQueue(id)
    })

    pendingList.value = pendingList.value.filter(item => item.status !== 'failed')
  }

  function getStats() {
    return offlineDB.getStats()
  }

  return {
    pendingList,
    syncStatus,
    syncProgress,
    syncResult,
    stats,
    loadPending,
    addPending,
    syncOne,
    syncAll,
    removeItem,
    retryItem,
    clearFailed,
    getStats
  }
})

export default useOfflineStore
