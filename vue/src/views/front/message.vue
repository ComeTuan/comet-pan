<template>
  <div class="main-container">
    <el-header height="0" style="margin-bottom:0px;"></el-header>
    <el-main >
      <div style="display: flex; margin: 10px 0;border-radius: 10px; border-color: #0d53ff;">
        <div style="width: 50%;" class="card">
          <div style="margin-bottom: 30px; font-size: 20px; font-weight: bold">公告列表</div>
          <div>
            <el-timeline  reverse slot="reference">
              <el-timeline-item v-for="item in notices" :key="item.id" :timestamp="item.time">
                <el-popover
                    placement="right"
                    width="200"
                    trigger="hover"
                    :content="item.content">
                  <span slot="reference">{{ item.title }}</span>
                </el-popover>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </div>
    </el-main>
    <el-footer height="0"></el-footer>
  </div>
</template>

<script>
import {setItemWithExpiry} from "@/App"
import {getItemWithExpiry} from "@/App"
import {updateItemWithExpiry} from "@/App"
import moment from 'moment';
export default {
  name: 'message',
  data() {
    return {
      user: getItemWithExpiry("user"),
      notices: []
    }
  },
  created() {
    this.$request.get('/notice/selectAll').then(res => {
      this.notices = res.data || []
      // 给每一个公告的时间戳添加 moment 格式化
      this.notices.forEach(item => {
        item.time = moment(item.time).format('YYYY-MM-DD HH:mm:ss');
      });
    })
  }
}

</script>

<style scoped>
/*.main-container {*/
/*  !*border-radius: 50px;*!*/
/*  border-top-left-radius: 50px;*/
/*  background-color: #ffffff;*/
/*  height: 100%;*/
/*  width: 100%;*/
/*}*/
.main-container {
margin: 0 auto;
width: 80%;
padding: 20px;
box-sizing: border-box;
}

.el-header {
  font-weight: bold;
  margin-bottom: 20px;
}

.el-main {
  height: 100%;
  padding: 20px;
  border-radius: 10px; /* 设置圆角 */
  border: 2px solid #0d53ff; /* 设置边框颜色和宽度 */
}

.notice-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notice-content {
  margin-bottom: 10px;
}

.no-notice {
  text-align: center;
  margin-top: 20px;
}

.popover:hover {
  color: #0d53ff;
}
</style>
