<template>
  <app-page-layout title="野外采集" :showBack="true">
    <view class="collection-page">
      <view class="type-selector">
        <text class="section-label">采集类型</text>
        <view class="type-grid">
          <view
            v-for="(t, i) in collectionTypes"
            :key="i"
            class="type-item"
            :class="{ active: formData.type === t.key }"
            @tap="selectType(t.key)">
            <text class="type-icon">{{ t.icon }}</text>
            <text class="type-name">{{ t.name }}</text>
          </view>
        </view>
      </view>

      <view class="card gps-card">
        <view class="card-header">
          <text class="card-title">📍 GPS定位</text>
          <view class="gps-btn" @tap="refreshLocation">
            <text class="gps-btn-text">{{ locating ? '定位中...' : '重新定位' }}</text>
          </view>
        </view>
        <view class="gps-info">
          <view class="gps-row">
            <text class="gps-label">经度</text>
            <text class="gps-value">{{ location.lng ? location.lng.toFixed(6) : '--' }}</text>
          </view>
          <view class="gps-row">
            <text class="gps-label">纬度</text>
            <text class="gps-value">{{ location.lat ? location.lat.toFixed(6) : '--' }}</text>
          </view>
          <view class="gps-row">
            <text class="gps-label">海拔</text>
            <text class="gps-value">{{ location.altitude ? location.altitude.toFixed(1) + ' m' : '--' }}</text>
          </view>
          <view class="gps-row">
            <text class="gps-label">精度</text>
            <text class="gps-value">{{ location.accuracy ? location.accuracy.toFixed(0) + ' m' : '--' }}</text>
          </view>
        </view>
      </view>

      <view class="card photo-card">
        <view class="card-header">
          <text class="card-title">📷 现场照片</text>
          <text class="photo-count">{{ photos.length }}/9</text>
        </view>
        <view class="photo-grid">
          <view
            v-for="(p, i) in photos"
            :key="i"
            class="photo-item">
            <image :src="p" mode="aspectFill" class="photo-img" @tap="previewPhoto(i)"></image>
            <view class="photo-delete" @tap="deletePhoto(i)">×</view>
          </view>
          <view v-if="photos.length < 9" class="photo-add" @tap="showActionSheet">
            <text class="add-icon">+</text>
            <text class="add-text">添加</text>
          </view>
        </view>
      </view>

      <view class="card form-card">
        <view class="card-header">
          <text class="card-title">📝 采集信息</text>
        </view>
        <view class="form-group">
          <text class="form-label">名称</text>
          <input class="form-input" v-model="formData.name" placeholder="请输入采集名称" placeholder-class="ph" />
        </view>
        <view class="form-group">
          <text class="form-label">描述</text>
          <textarea class="form-textarea" v-model="formData.description" placeholder="请输入详细描述" placeholder-class="ph"></textarea>
        </view>
        <view class="form-row">
          <view class="form-group half">
            <text class="form-label">深度(cm)</text>
            <input class="form-input" v-model="formData.depth" type="digit" placeholder="深度" placeholder-class="ph" />
          </view>
          <view class="form-group half">
            <text class="form-label">颜色</text>
            <input class="form-input" v-model="formData.color" placeholder="颜色描述" placeholder-class="ph" />
          </view>
        </view>
        <view class="form-group" v-if="formData.type === 'rock' || formData.type === 'soil'">
          <text class="form-label">硬度</text>
          <view class="hardness-selector">
            <view
              v-for="(h, i) in hardnessOptions"
              :key="i"
              class="hardness-item"
              :class="{ active: formData.hardness === h.value }"
              @tap="formData.hardness = h.value">
              {{ h.label }}
            </view>
          </view>
        </view>
      </view>

      <view class="card record-card">
        <view class="card-header">
          <text class="card-title">📋 今日采集</text>
          <text class="record-count">{{ todayRecords.length }} 条</text>
        </view>
        <view v-if="todayRecords.length === 0" class="empty-tip">暂无采集记录</view>
        <view v-else class="record-list">
          <view v-for="(r, i) in todayRecords" :key="i" class="record-item">
            <view class="record-icon">{{ getTypeIcon(r.type) }}</view>
            <view class="record-info">
              <text class="record-name">{{ r.name }}</text>
              <text class="record-time">{{ formatTime(r.createdAt) }}</text>
            </view>
            <view class="record-status" :class="r.syncStatus">
              {{ r.syncStatus === 'synced' ? '已同步' : '待同步' }}
            </view>
          </view>
        </view>
      </view>

      <view style="height: 160rpx;"></view>
    </view>

    <view class="bottom-bar">
      <button class="btn btn-outline" @tap="saveOffline">
        <text>💾 离线保存</text>
      </button>
      <button class="btn btn-primary" @tap="submitOnline">
        <text>📤 在线提交</text>
      </button>
    </view>
  </app-page-layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { toast, nav, formatDate } from '@/utils/index.js'
import locationUtil from '@/utils/location.js'
import cameraUtil from '@/utils/camera.js'
import offlineDB from '@/utils/offlineDB.js'
import { useOfflineStore } from '@/store/offline.js'

const offlineStore = useOfflineStore()
const locating = ref(false)
const photos = ref([])

const location = reactive({
  lat: null,
  lng: null,
  altitude: null,
  accuracy: null
})

const collectionTypes = [
  { key: 'soil', name: '土壤采样', icon: '🌱' },
  { key: 'rock', name: '岩石采样', icon: '🪨' },
  { key: 'disaster', name: '灾害记录', icon: '⚠️' },
  { key: 'device', name: '设备巡检', icon: '🔧' }
]

const hardnessOptions = [
  { label: '软', value: 'soft' },
  { label: '中', value: 'medium' },
  { label: '硬', value: 'hard' }
]

const formData = reactive({
  type: 'soil',
  name: '',
  description: '',
  depth: '',
  color: '',
  hardness: 'medium'
})

const todayRecords = ref([])

onMounted(() => {
  getLocation()
  loadTodayRecords()
})

function selectType(type) {
  formData.type = type
}

async function getLocation() {
  locating.value = true
  try {
    const loc = await locationUtil.getCurrentLocation()
    location.lat = loc.lat
    location.lng = loc.lng
    location.altitude = loc.altitude
    location.accuracy = loc.accuracy
  } catch (e) {
    toast.error('获取位置失败，请检查GPS设置')
  } finally {
    locating.value = false
  }
}

function refreshLocation() {
  getLocation()
}

function showActionSheet() {
  uni.showActionSheet({
    itemList: ['拍照', '从相册选择'],
    success: async (res) => {
      try {
        if (res.tapIndex === 0) {
          const path = await cameraUtil.takePhoto()
          photos.value.push(path)
        } else {
          const remain = 9 - photos.value.length
          const paths = await cameraUtil.chooseImage(remain)
          photos.value.push(...paths)
        }
      } catch (e) {
        // 用户取消
      }
    }
  })
}

function previewPhoto(index) {
  uni.previewImage({
    current: index,
    urls: photos.value
  })
}

function deletePhoto(index) {
  uni.showModal({
    title: '提示',
    content: '确定删除这张照片？',
    success: (res) => {
      if (res.confirm) {
        photos.value.splice(index, 1)
      }
    }
  })
}

function getCollectData() {
  return {
    ...formData,
    location: { ...location },
    photos: [...photos.value]
  }
}

function validateForm() {
  if (!formData.name.trim()) {
    toast.error('请输入采集名称')
    return false
  }
  if (!location.lat || !location.lng) {
    toast.error('请先获取GPS定位')
    return false
  }
  return true
}

async function saveOffline() {
  if (!validateForm()) return

  const data = getCollectData()
  const saved = offlineDB.save('field_collection', data)

  if (saved) {
    offlineStore.addPending({
      type: 'field_collection',
      data: saved,
      apiUrl: '/api/v1/field-collection'
    })

    toast.success('已保存到本地')
    resetForm()
    loadTodayRecords()
  } else {
    toast.error('保存失败')
  }
}

async function submitOnline() {
  if (!validateForm()) return

  toast.loading('提交中...')
  try {
    const data = getCollectData()
    const { request } = await import('@/api/index.js')
    await request({
      url: '/api/v1/field-collection',
      method: 'POST',
      data
    })
    toast.hide()
    toast.success('提交成功')
    resetForm()
    loadTodayRecords()
  } catch (e) {
    toast.hide()
    uni.showModal({
      title: '提交失败',
      content: '是否保存为离线数据，待网络恢复后同步？',
      success: (res) => {
        if (res.confirm) {
          saveOffline()
        }
      }
    })
  }
}

function resetForm() {
  formData.name = ''
  formData.description = ''
  formData.depth = ''
  formData.color = ''
  formData.hardness = 'medium'
  photos.value = []
}

function loadTodayRecords() {
  const all = offlineDB.getTable('field_collection')
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  todayRecords.value = all.filter(item => {
    const itemDate = new Date(item.createdAt)
    return itemDate >= today
  })
}

function getTypeIcon(type) {
  const map = { soil: '🌱', rock: '🪨', disaster: '⚠️', device: '🔧' }
  return map[type] || '📋'
}

function formatTime(timestamp) {
  return formatDate(timestamp, 'HH:mm')
}
</script>

<style lang="scss" scoped>
.collection-page {
  padding-bottom: 20rpx;
}

.type-selector {
  margin-bottom: 24rpx;
}

.section-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #5D4E37;
  margin-bottom: 16rpx;
  padding: 0 4rpx;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.type-item {
  background: linear-gradient(145deg, #FAFAF8, #F5F2ED);
  border-radius: 20rpx;
  padding: 24rpx 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(93, 78, 55, 0.06);

  &.active {
    background: linear-gradient(145deg, #F5EDE0, #EDE4D3);
    border-color: #C9A96E;
    box-shadow: 0 6rpx 18rpx rgba(201, 169, 110, 0.25);
  }
}

.type-icon {
  font-size: 44rpx;
  margin-bottom: 10rpx;
}

.type-name {
  font-size: 24rpx;
  color: #5D4E37;
  font-weight: 500;
}

.card {
  background: linear-gradient(145deg, #FAFAF8, #F5F2ED);
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(93, 78, 55, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #5D4E37;
}

.gps-btn {
  background: rgba(201, 169, 110, 0.15);
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
}

.gps-btn-text {
  font-size: 24rpx;
  color: #C9A96E;
  font-weight: 500;
}

.gps-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.gps-row {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
}

.gps-label {
  display: block;
  font-size: 22rpx;
  color: #8B7355;
  margin-bottom: 6rpx;
}

.gps-value {
  font-size: 28rpx;
  color: #5D4E37;
  font-weight: 600;
  font-family: monospace;
}

.photo-count {
  font-size: 24rpx;
  color: #8B7355;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.photo-item {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  border-radius: 16rpx;
  overflow: hidden;
}

.photo-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.photo-delete {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  line-height: 1;
}

.photo-add {
  width: 100%;
  padding-bottom: 100%;
  position: relative;
  background: rgba(255, 255, 255, 0.7);
  border: 2rpx dashed #D8CFBE;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.add-icon,
.add-text {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.add-icon {
  top: 30%;
  font-size: 48rpx;
  color: #B5A896;
}

.add-text {
  bottom: 25%;
  font-size: 24rpx;
  color: #8B7355;
}

.form-group {
  margin-bottom: 20rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.form-row {
  display: flex;
  gap: 20rpx;
}

.form-group.half {
  flex: 1;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #5D4E37;
  font-weight: 500;
  margin-bottom: 10rpx;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #5D4E37;
  box-sizing: border-box;
  border: 2rpx solid #F0EBE2;
}

.form-textarea {
  width: 100%;
  height: 160rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  color: #5D4E37;
  box-sizing: border-box;
  border: 2rpx solid #F0EBE2;
}

.ph {
  color: #B5A896;
}

.hardness-selector {
  display: flex;
  gap: 16rpx;
}

.hardness-item {
  flex: 1;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #8B7355;
  border: 2rpx solid #F0EBE2;
  transition: all 0.3s ease;

  &.active {
    background: rgba(201, 169, 110, 0.15);
    border-color: #C9A96E;
    color: #C9A96E;
    font-weight: 600;
  }
}

.record-count {
  font-size: 24rpx;
  color: #8B7355;
}

.empty-tip {
  text-align: center;
  padding: 40rpx 0;
  color: #B5A896;
  font-size: 26rpx;
}

.record-list {
  max-height: 400rpx;
  overflow-y: auto;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F0EBE2;

  &:last-child {
    border-bottom: none;
  }
}

.record-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.record-name {
  font-size: 28rpx;
  color: #5D4E37;
  font-weight: 500;
}

.record-time {
  font-size: 22rpx;
  color: #B5A896;
}

.record-status {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;

  &.synced {
    background: rgba(124, 179, 66, 0.15);
    color: #7CB342;
  }

  &:not(.synced) {
    background: rgba(201, 169, 110, 0.15);
    color: #C9A96E;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FEFBF6;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -4rpx 16rpx rgba(93, 78, 55, 0.08);
  z-index: 100;
}

.btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  line-height: 1;
}

.btn-outline {
  background: transparent;
  color: #C9A96E;
  border: 2rpx solid #C9A96E;
}

.btn-primary {
  background: linear-gradient(135deg, #D9C49A 0%, #C9A96E 100%);
  color: #fff;
  box-shadow: 0 6rpx 20rpx rgba(201, 169, 110, 0.35);
}
</style>
