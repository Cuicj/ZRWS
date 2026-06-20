<template>
  <app-page-layout title="土壤采样" :showBack="true" :showTabbar="true" :refresherEnabled="true" @refresh="loadData">
    <!-- 采样统计 -->
    <view class="stats-bar">
      <view class="stat-item">
        <text class="stat-num">{{ stats.total }}</text>
        <text class="stat-label">采样总数</text>
      </view>
      <view class="stat-item">
        <text class="stat-num">{{ stats.today }}</text>
        <text class="stat-label">今日新增</text>
      </view>
      <view class="stat-item">
        <text class="stat-num">{{ avgPh }}</text>
        <text class="stat-label">平均pH</text>
      </view>
    </view>

    <!-- 新建采样表单 -->
    <view class="form-card">
      <view class="form-head">
        <text class="form-title">📝 新建采样记录</text>
        <text class="form-loc" @tap="getLocation">
          📍 自动定位
        </text>
      </view>

      <view class="form-group">
        <text class="form-label">采样点编号 *</text>
        <input class="form-input" v-model="form.id" placeholder="如 SP-2026-0618-013" />
      </view>

      <view class="form-row">
        <view class="form-item half">
          <text class="form-label">经度</text>
          <input class="form-input" v-model="form.lng" placeholder="112.835210" type="digit" />
        </view>
        <view class="form-item half">
          <text class="form-label">纬度</text>
          <input class="form-input" v-model="form.lat" placeholder="28.456720" type="digit" />
        </view>
      </view>

      <view class="form-row">
        <view class="form-item third">
          <text class="form-label">pH值 *</text>
          <input class="form-input" v-model.number="form.ph" placeholder="如 6.8" type="digit" />
        </view>
        <view class="form-item third">
          <text class="form-label">水分(%)</text>
          <input class="form-input" v-model.number="form.moisture" placeholder="如 32.5" type="digit" />
        </view>
        <view class="form-item third">
          <text class="form-label">EC(电导率)</text>
          <input class="form-input" v-model.number="form.ec" placeholder="如 145" type="digit" />
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">土质类型</text>
        <view class="type-options">
          <text
            v-for="t in soilTypes"
            :key="t"
            class="type-tag"
            :class="{active: form.type === t}"
            @tap="form.type = t">{{ t }}</text>
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">备注</text>
        <textarea
          class="form-textarea"
          v-model="form.note"
          placeholder="现场观察记录..."
          :auto-height="true"></textarea>
      </view>

      <button class="form-submit" @tap="saveSample">💾 保存采样数据</button>
    </view>

    <!-- 历史采样列表 -->
    <view class="history-head">
      <text class="hh-title">📋 最近采样 ({{ samples.length }})</text>
    </view>
    <view v-if="samples.length === 0" class="empty-state">暂无采样数据</view>
    <view v-for="(s, i) in samples" :key="i" class="sample-card">
      <view class="sample-head">
        <text class="sample-id">{{ s.id }}</text>
        <text class="sample-type" :class="'type-' + (soilTypes.indexOf(s.type) % 4)">{{ s.type }}</text>
      </view>
      <view class="sample-coord">
        📍 {{ s.lng }}°, {{ s.lat }}°
      </view>
      <view class="sample-data">
        <view class="sd-item">
          <text class="sd-label">pH</text>
          <text class="sd-value">{{ s.ph }}</text>
        </view>
        <view class="sd-item">
          <text class="sd-label">水分</text>
          <text class="sd-value">{{ s.moisture }}%</text>
        </view>
        <view class="sd-item">
          <text class="sd-label">EC</text>
          <text class="sd-value">{{ s.ec }}</text>
        </view>
      </view>
      <view v-if="s.note" class="sample-note">📝 {{ s.note }}</view>
    </view>

    <view style="height: 40rpx;"></view>
  </app-page-layout>
</template>

<script setup>
  import { ref, reactive, onMounted, computed } from 'vue'
  import { mockSoilSamples } from '@/utils/mock.js'
  import { toast } from '@/utils/index.js'

  const soilTypes = ['壤土', '黏土', '砂土', '粉砂土', '泥炭土', '盐碱土']
  const samples = ref([])
  const stats = reactive({ total: 0, today: 0 })

  const form = reactive({
    id: '',
    lng: '112.835210',
    lat: '28.456720',
    ph: '',
    moisture: '',
    ec: '',
    type: '壤土',
    note: ''
  })

  const avgPh = computed(() => {
    if (samples.value.length === 0) return '-'
    const sum = samples.value.reduce((acc, s) => acc + (parseFloat(s.ph) || 0), 0)
    return (sum / samples.value.length).toFixed(1)
  })

  onMounted(() => loadData())

  function loadData() {
    // 加载 mock 数据
    setTimeout(() => {
      samples.value = [...mockSoilSamples]
      stats.total = samples.value.length
      stats.today = Math.min(5 + Math.floor(Math.random() * 10), samples.value.length)
      // 预填编号
      form.id = 'SP-' + new Date().toISOString().slice(0,10).replace(/-/g,'') + '-' + String(samples.value.length + 1).padStart(3, '0')
    }, 200)
  }

  function getLocation() {
    // 模拟定位
    uni.showLoading({ title: '定位中...' })
    setTimeout(() => {
      uni.hideLoading()
      form.lng = (112.835 + (Math.random() - 0.5) * 0.01).toFixed(6)
      form.lat = (28.456 + (Math.random() - 0.5) * 0.01).toFixed(6)
      toast.success('定位成功')
    }, 800)
  }

  function saveSample() {
    if (!form.id) {
      toast.info('请填写采样点编号')
      return
    }
    if (!form.ph) {
      toast.info('请填写pH值')
      return
    }
    samples.value.unshift({
      id: form.id,
      lng: form.lng,
      lat: form.lat,
      ph: form.ph,
      moisture: form.moisture || 0,
      ec: form.ec || 0,
      type: form.type,
      note: form.note
    })
    stats.total = samples.value.length
    stats.today++
    toast.success('保存成功')

    // 清空部分字段，便于继续采样
    const nextSeq = parseInt(form.id.split('-').pop()) + 1
    form.id = 'SP-' + new Date().toISOString().slice(0,10).replace(/-/g,'') + '-' + String(nextSeq).padStart(3, '0')
    form.ph = ''
    form.moisture = ''
    form.ec = ''
    form.note = ''
  }
</script>

<style lang="scss" scoped>
  .stats-bar {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 20rpx;
    margin-bottom: 24rpx;
  }
  .stat-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx 16rpx;
    text-align: center;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .stat-num {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    color: #2b6cb0;
    font-family: 'DIN', monospace;
    margin-bottom: 6rpx;
  }
  .stat-label {
    font-size: 22rpx;
    color: #909399;
  }

  .form-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .form-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    padding-bottom: 20rpx;
    border-bottom: 1rpx solid #f0f0f0;
  }
  .form-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #303133;
  }
  .form-loc {
    font-size: 24rpx;
    color: #2b6cb0;
    background: #ecf5ff;
    padding: 8rpx 18rpx;
    border-radius: 20rpx;
  }

  .form-group {
    margin-bottom: 24rpx;
  }
  .form-label {
    display: block;
    font-size: 26rpx;
    color: #606266;
    margin-bottom: 12rpx;
  }
  .form-input {
    width: 100%;
    height: 80rpx;
    background: #f5f7fa;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #303133;
  }
  .form-textarea {
    width: 100%;
    min-height: 120rpx;
    background: #f5f7fa;
    border-radius: 12rpx;
    padding: 20rpx 24rpx;
    font-size: 26rpx;
    color: #303133;
  }
  .form-row {
    display: flex;
    gap: 16rpx;
    margin-bottom: 24rpx;
  }
  .form-item.half {
    flex: 1;
  }
  .form-item.third {
    flex: 1;
  }

  .type-options {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }
  .type-tag {
    padding: 12rpx 28rpx;
    background: #f0f2f5;
    border-radius: 24rpx;
    font-size: 24rpx;
    color: #606266;
  }
  .type-tag.active {
    background: linear-gradient(135deg, #1e3a5f, #2b6cb0);
    color: #fff;
    font-weight: 600;
  }

  .form-submit {
    width: 100%;
    height: 90rpx;
    background: linear-gradient(135deg, #67c23a, #4299e1);
    color: #fff;
    font-size: 30rpx;
    font-weight: 600;
    border-radius: 12rpx;
    border: none;
    margin-top: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .history-head {
    padding: 16rpx 4rpx;
  }
  .hh-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
  }

  .empty-state {
    text-align: center;
    padding: 80rpx 40rpx;
    color: #c0c4cc;
    font-size: 26rpx;
    background: #fff;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
  }

  .sample-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  }
  .sample-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12rpx;
  }
  .sample-id {
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
  }
  .sample-type {
    font-size: 22rpx;
    padding: 6rpx 18rpx;
    border-radius: 20rpx;
    background: #f0f2f5;
    color: #606266;
  }
  .sample-type.type-0 { background: #ecf5ff; color: #2b6cb0; }
  .sample-type.type-1 { background: #fdf6ec; color: #e6a23c; }
  .sample-type.type-2 { background: #f0f9eb; color: #67c23a; }
  .sample-type.type-3 { background: #fef0f0; color: #f56c6c; }

  .sample-coord {
    font-size: 22rpx;
    color: #909399;
    font-family: 'DIN', monospace;
    margin-bottom: 16rpx;
  }
  .sample-data {
    display: flex;
    gap: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx dashed #f0f0f0;
  }
  .sd-item {
    flex: 1;
    background: #f5f7fa;
    border-radius: 10rpx;
    padding: 14rpx;
    text-align: center;
  }
  .sd-label {
    display: block;
    font-size: 20rpx;
    color: #909399;
    margin-bottom: 6rpx;
  }
  .sd-value {
    display: block;
    font-size: 26rpx;
    font-weight: 600;
    color: #303133;
    font-family: 'DIN', monospace;
  }
  .sample-note {
    font-size: 22rpx;
    color: #606266;
    margin-top: 12rpx;
    padding-top: 12rpx;
    border-top: 1rpx dashed #f0f0f0;
  }
</style>
