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

      <view class="form-submit-wrap">
        <zrws-button variant="primary" size="lg" block @click="saveSample">💾 保存采样数据</zrws-button>
      </view>
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
  import { soilApi } from '@/api/index.js'
  import { toast } from '@/utils/index.js'

  const soilTypes = ['壤土', '黏土', '砂土', '粉砂土', '泥炭土', '盐碱土']
  const samples = ref([])
  const loading = ref(false)
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

  async function loadData() {
    loading.value = true
    try {
      const [listRes, statsRes] = await Promise.all([
        soilApi.list({ pageSize: 50 }).catch(() => null),
        soilApi.stats().catch(() => null)
      ])

      if (listRes) {
        samples.value = listRes.list || listRes.records || listRes || []
      }

      if (statsRes) {
        stats.total = statsRes.total || statsRes.totalSamples || samples.value.length
        stats.today = statsRes.today || statsRes.todaySamples || 0
      } else {
        stats.total = samples.value.length
      }

      // 预填编号
      const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '')
      form.id = 'SP-' + dateStr + '-' + String(samples.value.length + 1).padStart(3, '0')
    } catch (e) {
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
  }

  function getLocation() {
    uni.showLoading({ title: '定位中...' })
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        uni.hideLoading()
        form.lng = res.longitude.toFixed(6)
        form.lat = res.latitude.toFixed(6)
        toast.success('定位成功')
      },
      fail: () => {
        uni.hideLoading()
        // 定位失败时使用默认值
        toast.info('定位失败，使用默认位置')
      }
    })
  }

  async function saveSample() {
    if (!form.id) {
      toast.info('请填写采样点编号')
      return
    }
    if (!form.ph) {
      toast.info('请填写pH值')
      return
    }

    loading.value = true
    try {
      const sampleData = {
        sampleId: form.id,
        lng: form.lng,
        lat: form.lat,
        ph: parseFloat(form.ph),
        moisture: parseFloat(form.moisture) || 0,
        ec: parseFloat(form.ec) || 0,
        soilType: form.type,
        remark: form.note
      }
      await soilApi.create(sampleData)

      toast.success('保存成功')
      loadData()

      // 清空部分字段，便于继续采样
      const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '')
      const nextSeq = samples.value.length + 2
      form.id = 'SP-' + dateStr + '-' + String(nextSeq).padStart(3, '0')
      form.ph = ''
      form.moisture = ''
      form.ec = ''
      form.note = ''
    } catch (e) {
      // 错误提示已在 request 封装中处理
    } finally {
      loading.value = false
    }
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
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 28rpx 16rpx;
    text-align: center;
    box-shadow: $zrws-shadow-sm;
  }
  .stat-num {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    color: $zrws-primary;
    font-family: 'DIN', monospace;
    margin-bottom: 6rpx;
  }
  .stat-label {
    font-size: 22rpx;
    color: $zrws-text-tertiary;
  }

  .form-card {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 28rpx;
    margin-bottom: 24rpx;
    box-shadow: $zrws-shadow-sm;
  }
  .form-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    padding-bottom: 20rpx;
    border-bottom: 1rpx solid $zrws-border-light;
  }
  .form-title {
    font-size: 30rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }
  .form-loc {
    font-size: 24rpx;
    color: $zrws-primary-dark;
    background: $zrws-primary-bg;
    padding: 8rpx 18rpx;
    border-radius: 20rpx;
  }

  .form-group {
    margin-bottom: 24rpx;
  }
  .form-label {
    display: block;
    font-size: 26rpx;
    color: $zrws-text-secondary;
    margin-bottom: 12rpx;
  }
  .form-input {
    width: 100%;
    height: 80rpx;
    background: $zrws-bg-tertiary;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: $zrws-text-primary;
  }
  .form-textarea {
    width: 100%;
    min-height: 120rpx;
    background: $zrws-bg-tertiary;
    border-radius: 12rpx;
    padding: 20rpx 24rpx;
    font-size: 26rpx;
    color: $zrws-text-primary;
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
    background: $zrws-bg-tertiary;
    border-radius: 24rpx;
    font-size: 24rpx;
    color: $zrws-text-secondary;
  }
  .type-tag.active {
    background: linear-gradient(135deg, $zrws-primary-dark, $zrws-primary);
    color: $zrws-text-inverse;
    font-weight: 600;
  }

  .form-submit-wrap {
    margin-top: 20rpx;
  }

  .history-head {
    padding: 16rpx 4rpx;
  }
  .hh-title {
    font-size: 28rpx;
    font-weight: 600;
    color: $zrws-text-primary;
  }

  .empty-state {
    text-align: center;
    padding: 80rpx 40rpx;
    color: $zrws-text-tertiary;
    font-size: 26rpx;
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    margin-bottom: 20rpx;
  }

  .sample-card {
    background: $zrws-bg-secondary;
    border-radius: $zrws-radius-md;
    padding: 24rpx;
    margin-bottom: 16rpx;
    box-shadow: $zrws-shadow-sm;
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
    color: $zrws-text-primary;
  }
  .sample-type {
    font-size: 22rpx;
    padding: 6rpx 18rpx;
    border-radius: 20rpx;
    background: $zrws-bg-tertiary;
    color: $zrws-text-secondary;
  }
  .sample-type.type-0 { background: $zrws-primary-bg; color: $zrws-primary-dark; }
  .sample-type.type-1 { background: rgba($zrws-warning, 0.1); color: $zrws-warning; }
  .sample-type.type-2 { background: rgba($zrws-success, 0.1); color: $zrws-success; }
  .sample-type.type-3 { background: rgba($zrws-error, 0.1); color: $zrws-error; }

  .sample-coord {
    font-size: 22rpx;
    color: $zrws-text-tertiary;
    font-family: 'DIN', monospace;
    margin-bottom: 16rpx;
  }
  .sample-data {
    display: flex;
    gap: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx dashed $zrws-border-light;
  }
  .sd-item {
    flex: 1;
    background: $zrws-bg-tertiary;
    border-radius: 10rpx;
    padding: 14rpx;
    text-align: center;
  }
  .sd-label {
    display: block;
    font-size: 20rpx;
    color: $zrws-text-tertiary;
    margin-bottom: 6rpx;
  }
  .sd-value {
    display: block;
    font-size: 26rpx;
    font-weight: 600;
    color: $zrws-text-primary;
    font-family: 'DIN', monospace;
  }
  .sample-note {
    font-size: 22rpx;
    color: $zrws-text-secondary;
    margin-top: 12rpx;
    padding-top: 12rpx;
    border-top: 1rpx dashed $zrws-border-light;
  }
</style>
