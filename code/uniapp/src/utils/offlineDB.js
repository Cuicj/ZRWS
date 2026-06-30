const DB_PREFIX = 'zrws_db_'
const SYNC_QUEUE_KEY = 'zrws_sync_queue'

function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2, 9)
}

export function getTable(tableName) {
  try {
    const key = DB_PREFIX + tableName
    const data = uni.getStorageSync(key)
    return data ? JSON.parse(data) : []
  } catch (e) {
    console.error('getTable error:', e)
    return []
  }
}

export function save(tableName, data) {
  try {
    const key = DB_PREFIX + tableName
    const list = getTable(tableName)
    const item = {
      ...data,
      id: data.id || generateId(),
      createdAt: data.createdAt || Date.now(),
      updatedAt: Date.now()
    }
    list.unshift(item)
    uni.setStorageSync(key, JSON.stringify(list))
    return item
  } catch (e) {
    console.error('save error:', e)
    return null
  }
}

export function update(tableName, id, data) {
  try {
    const key = DB_PREFIX + tableName
    const list = getTable(tableName)
    const index = list.findIndex(item => item.id === id)
    if (index > -1) {
      list[index] = {
        ...list[index],
        ...data,
        id,
        updatedAt: Date.now()
      }
      uni.setStorageSync(key, JSON.stringify(list))
      return list[index]
    }
    return null
  } catch (e) {
    console.error('update error:', e)
    return null
  }
}

export function remove(tableName, id) {
  try {
    const key = DB_PREFIX + tableName
    const list = getTable(tableName)
    const filtered = list.filter(item => item.id !== id)
    uni.setStorageSync(key, JSON.stringify(filtered))
    return true
  } catch (e) {
    console.error('remove error:', e)
    return false
  }
}

export function clear(tableName) {
  try {
    const key = DB_PREFIX + tableName
    uni.setStorageSync(key, JSON.stringify([]))
    return true
  } catch (e) {
    console.error('clear error:', e)
    return false
  }
}

export function addToSyncQueue(item) {
  try {
    const queue = getSyncQueue()
    const syncItem = {
      ...item,
      syncId: item.syncId || generateId(),
      status: 'pending',
      retryCount: 0,
      createdAt: item.createdAt || Date.now()
    }
    queue.unshift(syncItem)
    uni.setStorageSync(SYNC_QUEUE_KEY, JSON.stringify(queue))
    return syncItem
  } catch (e) {
    console.error('addToSyncQueue error:', e)
    return null
  }
}

export function getSyncQueue() {
  try {
    const data = uni.getStorageSync(SYNC_QUEUE_KEY)
    return data ? JSON.parse(data) : []
  } catch (e) {
    console.error('getSyncQueue error:', e)
    return []
  }
}

export function removeFromSyncQueue(syncId) {
  try {
    const queue = getSyncQueue()
    const filtered = queue.filter(item => item.syncId !== syncId)
    uni.setStorageSync(SYNC_QUEUE_KEY, JSON.stringify(filtered))
    return true
  } catch (e) {
    console.error('removeFromSyncQueue error:', e)
    return false
  }
}

export function updateSyncQueueItem(syncId, data) {
  try {
    const queue = getSyncQueue()
    const index = queue.findIndex(item => item.syncId === syncId)
    if (index > -1) {
      queue[index] = {
        ...queue[index],
        ...data,
        syncId,
        updatedAt: Date.now()
      }
      uni.setStorageSync(SYNC_QUEUE_KEY, JSON.stringify(queue))
      return queue[index]
    }
    return null
  } catch (e) {
    console.error('updateSyncQueueItem error:', e)
    return null
  }
}

export function getStats() {
  const queue = getSyncQueue()
  return {
    total: queue.length,
    pending: queue.filter(item => item.status === 'pending').length,
    syncing: queue.filter(item => item.status === 'syncing').length,
    failed: queue.filter(item => item.status === 'failed').length,
    success: queue.filter(item => item.status === 'success').length
  }
}

export default {
  getTable,
  save,
  update,
  remove,
  clear,
  addToSyncQueue,
  getSyncQueue,
  removeFromSyncQueue,
  updateSyncQueueItem,
  getStats
}
