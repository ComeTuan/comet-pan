<template>
      <div>
        <div class="search">
          <el-input placeholder="请输入文件名查询" style="width: 200px" v-model="fileName"></el-input>
          <el-button type="info" plain style="margin-left: 10px" @click="load()">查询</el-button>
          <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
        </div>

        <div class="operation">
<!--          <el-button type="primary" plain @click="handleAdd">上传文件</el-button>-->
          <el-button type="danger" plain @click="delBatch">批量删除</el-button>
        </div>

        <div class="table">
          <el-table :data="tableData">
            <el-table-column type="selection" width="55" align="center"></el-table-column>
            <el-table-column prop="id" label="ID" width="70" align="center" sortable></el-table-column>
            <el-table-column prop="name" label="文件名"></el-table-column>
            <el-table-column prop="size" label="文件大小" :formatter="formatSize"></el-table-column>
            <el-table-column prop="path" label="文件路径"></el-table-column>
            <el-table-column prop="createTime" label="上传时间" :formatter="formatTime"></el-table-column>
            <el-table-column prop="updateTime" label="更新时间" :formatter="formatTime"></el-table-column>
            <el-table-column label="操作" align="center" width="180">
              <template v-slot="scope">
<!--                <el-button size="mini" type="primary" plain @click="handleDownload(scope.row)">下载</el-button>-->
                <el-button size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
                background
                @current-change="handleCurrentChange"
                @size-change="handleSizeChange"
                :current-page="pageNum"
                :page-sizes="[5, 10, 20]"
                :page-size.sync="pageSize"
                layout="total, prev, pager, next, sizes,jumper"
                :total="total">
            </el-pagination>
          </div>
        </div>

        <el-dialog title="上传文件" :visible.sync="uploadVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
          <el-upload class="upload-demo" ref="upload" action="/files/upload" :auto-upload="false" :on-change="handleChange" :file-list="fileList" >
            <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传到服务器</el-button>
          </el-upload>
        </el-dialog>
      </div>
</template>

<script>

import { getItemWithExpiry } from "@/App";
import { updateItemWithExpiry } from "@/App";
import { setItemWithExpiry } from "@/App";
import moment from "moment";

export default {
  name: "FileManager",
  data() {
    return {
      tableData: [], // 所有的数据
      pageNum: 1, // 当前的页码
      pageSize: 10, // 每页显示的个数
      total: 0,
      fileName: null,
      uploadVisible: false,
      fileList: [],
      rules: {
        fileName: [
          { required: true, message: '请选择文件', trigger: 'change' },
        ],
      },
      ids: [],
      file: {},
      folderId: -1,
    };
  },
  mounted() {
    this.load();
  },
  computed: {
    filteredData() {
      let data = this.tableData
      // this.total = data.length
      // this.getPath(this.folderId)
      return data;
    },
  },
  methods: {
    // 打开上传对话框
    handleAdd() {
      this.uploadVisible = true;
    },
    // 下载单个文件
    handleDownload(row) {
      // 实现下载逻辑，可能需要调用后端接口或者使用 a 标签的 download 属性
      // 这里只是一个示例，具体实现需要根据项目实际情况
      const url = `/files/download/${row.id}`;
      const link = document.createElement('a');
      link.href = url;
      link.download = row.fileName; // 文件名
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    // 提交上传操作
    submitUpload() {
      this.$refs.upload.submit(); // 调用 el-upload 的 submit 方法
    },
    // 处理文件选择变化
    handleChange(file, fileList) {
      this.fileList = fileList; // 更新文件列表
    },
    // 处理当前页变化
    handleCurrentChange(newPage) {
      this.pageNum = newPage; // 更新页码
      this.load(); // 重新加载数据
    },
    // 处理每页显示个数变化
    handleSizeChange(newSize) {
      this.pageSize = newSize; // 更新每页显示个数
      this.load(); // 重新加载数据
    },
    // 加载数据的方法
    load() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        name: this.fileName // 如果有文件名查询，则添加到参数中
      };
      // 调用后端接口获取数据
      this.$request.get('/files/selectAll', { params }).then(res => {
        if (res.code === '200') {
          this.tableData = res.data; // 更新表格数据
          this.total = res.data.total; // 更新总记录数
          console.log(this.tableData)
        } else {
          this.$message.error;
        }
      });
    },
    formatSize(row, column) {
      // row 是当前行的数据对象
      // column 是当前列的属性配置对象
      // 调用过滤器来格式化文件大小
      return this.$options.filters.sizeFormat(row[column.property], 1);
    },
    formatTime(row, column) {
      return this.$options.filters.formatTime(row[column.property]);
    },
    reset() {
      this.fileName = null
      this.load(1)
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('请选择')
        return
      }
      this.$confirm('确定要删除选中的文件吗？', '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/files', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('删除成功')
            this.reload()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
  },
};
</script>