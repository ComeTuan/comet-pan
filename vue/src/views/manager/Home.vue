<template>
  <div>
    <div class="card" style="padding: 15px">
      您好，{{ user?.name }}！欢迎使用本系统
    </div>

    <div style="display: flex; margin: 10px 0;justify-content: center">
      <div style="width: 100%;" class="card">
        <div style="margin-bottom: 30px; font-size: 20px; font-weight: bold;text-align: center;">数据看板</div>
        <div class="m-box">
          <DIV class="container" align=center style="height: 100%;width: 888px;margin: auto;"><IFRAME
              src="http://192.168.245.1:10000/nevnztvy5m47/"
              frameBorder=0 marginwidth=0 marginheight=0 scrolling=no
              style="width:100%;height:100%"
              ALLOWTRANSPARENCY="true"></IFRAME></DIV>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {setItemWithExpiry} from "@/App"
import {getItemWithExpiry} from "@/App"
import {updateItemWithExpiry} from "@/App"
export default {
  name: 'Home',
  data() {
    return {
      user: getItemWithExpiry("user"),
      notices: []
    }
  },
  created() {
    this.$request.get('/notice/selectAll').then(res => {
      this.notices = res.data || []
    })
  }
}
</script>
