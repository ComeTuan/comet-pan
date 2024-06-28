<template>
  <div class="main-container">
    <div class="blank"></div>
    <div class="operation">
      <el-input class="search-input" clearable placeholder="搜索全部文件" style="width:20% " v-model="name" prefix-icon="el-icon-search"
                @keyup.enter.native="selectAll(1)">
      </el-input>
      <el-button class="search-button" type="text" plain style="margin-left: 1rem" @click="selectAll(1)">搜索</el-button>
      <el-dropdown class="select-dropdown" @command="handleFileTypeChange">
        <el-button class="search-button" type="primary">
          文件筛选<i class="el-icon-arrow-down el-icon--right"></i>
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="ALL">全部文件</el-dropdown-item>
          <el-dropdown-item command="Text File">文本文件</el-dropdown-item>
          <el-dropdown-item command="Image">图片文件</el-dropdown-item>
          <el-dropdown-item command="Video">视频文件</el-dropdown-item>
          <el-dropdown-item command="Compressed File">压缩文件</el-dropdown-item>
          <el-dropdown-item command="PDF File">PDF文件</el-dropdown-item>
          <el-dropdown-item command="Excel File">Excel文件</el-dropdown-item>
          <el-dropdown-item command="Word File">Word文件</el-dropdown-item>
          <el-dropdown-item command="Code File">代码文件</el-dropdown-item>
          <el-dropdown-item command="Executable File">可执行文件</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button class="refresh-button" type="success" plain style="margin-left: 1rem" @click="reset">
        <i class="el-icon-refresh"></i> 刷新
      </el-button>
      <!--      <img class="little-icon" src="@/assets/imgs/folder-add.svg" alt="">-->
      <el-button class="primary-button" type="text" plain style="margin-left: 1rem" @click="addFolder">
        <i class="el-icon-plus"></i> 新建文件夹
      </el-button>
      <el-button class="normal-button" type="danger" plain @click="delBatch">
        <i class="el-icon-delete-solid"></i> 批量删除
      </el-button>
      <el-button class="primary-button" type="primary" plain style="margin-left: 1rem" @click="uploadFile">
        <i class="el-icon-upload"></i> 上传 / 秒传
      </el-button>
      <el-button class="normal-button" type="info" plain style="margin-left: 1rem"
                 @click="shareFile">
        <i class="el-icon-share"></i> 分享
      </el-button>
      <el-button class="warning-button" type="warning" plain style="margin-left: 1rem"
                 @click="downloadBatch">
        <i class="el-icon-download"></i> 下载
      </el-button>
      <el-button type="primary" @click="showDialog" class="aibutton" style="margin-left: 15px; margin-top: 3px;">AI</el-button>
    </div>
    <el-dialog :visible.sync="dialogAIVisible" center title="AI智能助手" :close-on-click-modal="false">
      <template #title>
        <div style="display: flex; align-items: center; justify-content: center;">
          <h style="font-size: 25px">AI智能助手</h>
          <el-avatar :size="35" :src="avatarSrc"></el-avatar>
        </div>
      </template>
      <el-container>
        <el-header height="0">
        </el-header>
        <el-main class="AIdialogmain">
          <!-- 消息列表 -->
<!--          <div class="message-list">-->
<!--            <el-card v-for="(message, index) in messages" :key="index">-->
<!--              {{ message }}-->
<!--            </el-card>-->
<!--          </div>-->
          <!-- 消息列表 -->
          <div class="message-list" ref="messageList">
            <!-- 用户和AI消息 -->
            <div v-for="(message, index) in messages" :key="index" :class="{'user-message': index % 2 !== 0, 'ai-message': index % 2 === 0}">
              <!-- 用户消息 -->
              <div v-if="index % 2 !== 0" class="user-message-container">
                <div class="message-box">
                  {{ message }}
                </div>
                <el-avatar class="avatar" :src="$baseUrl+'/avatar/'+user.id" shape="circle" fit="contain" size="large" alt=""></el-avatar>
              </div>

              <!-- AI消息 -->
              <div v-else class="ai-message-container">
                <el-avatar :size="35" :src="avatarSrc"></el-avatar>
                <div class="message-box">
                  {{ message }}
                </div>
              </div>
            </div>
          </div>
        </el-main>
        <el-footer class="AIdialogfoot">
          <!-- 输入框 -->
          <el-input class="aiinput" v-model="inputMessage" :placeholder=placeholder :disabled="processingAI" size="large"  clearable style="background-color: #f0f0f0;">{{inputMessage}}</el-input>
          <!-- 发送按钮 -->
          <el-button class="submit-button"  @click="sendMessage" style="margin-left: 1%;" :disabled="processingAI">发送</el-button>
          <el-button class="submit-button"  @click="setMessages">示例</el-button>
        </el-footer>
      </el-container>
    </el-dialog>

    <div class="blank"></div>
    <div class="backAndForward">
      <el-button type="primary" plain @click="backNavigation" icon="el-icon-back" class="navigation-button"
                 circle :disabled="cacheIndex <= 0"></el-button>
      <el-button type="primary" plain @click="forwardNavigation" icon="el-icon-right" class="navigation-button"
                 circle :disabled="cacheIndex >= requestCache.length - 1"></el-button>
      <div class="path">
        <span v-if="!isSearch">全部文件</span>
        <span v-else>搜索结果</span>
        <span>{{ this.path }} 共<h style="color: #0d53ff">{{ this.total }}</h>个文件</span>
      </div>
    </div>

    <div class="table">
      <!-- 使用 v-if 控制 el-skeleton 加载动画的显示与隐藏 -->
      <el-skeleton class="table-skeleton" :rows="10" animated v-if="loading"/>
      <!--文件列表-->

      <el-table v-else :data="filteredData" strip @selection-change="handleSelectionChange"
                height="68vh" class="table-style" empty-text="" @row-contextmenu="rightClick"
                ref="table" :default-sort="{prop: 'name', order: 'ascending'}"
                @cell-mouse-enter="mouseEnter"
                @cell-mouse-leave="mouseLeave"
                :row-style="{height: '4rem'}"
                :cell-style="{padding: '0'}"
      >
        <!--无文件-->
        <template v-if="!isSearch" slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 1.2rem;font-weight: bold;'>这里还没有文件哦, 赶快上传吧</span>
            </p>
          </el-empty>
          <el-button type="primary" @click="uploadFile" class="primary-button">上传 / 秒传</el-button>
        </template>
        <!--搜索文件为空-->
        <template v-else slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 1.1rem;font-weight: bold'>没有找到相关文件</span></p>
          </el-empty>
        </template>

        <!--复选框列-->
        <el-table-column type="selection" min-width="30" align="center"></el-table-column>
        <!--文件图标-->
        <el-table-column prop="folder" label="" width="60">
          <template v-slot="scope">
            <span @click="handleFolderClick(scope.row)" style="cursor: pointer;">
              <FileIcon :file-type="scope.row.type" :isFolder="scope.row.folder"></FileIcon>
            </span>
          </template>
        </el-table-column>
        <!--文件信息-->
        <el-table-column prop="name" label="文件名称" min-width="180" show-overflow-tooltip
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']"
        >
          <template v-slot="scope">
<!--            搜索高亮-->
            <template v-if="!scope.row.isEditing">
              <div class="name-container" @click="handleFolderClick(scope.row)">
                <!-- 如果type为空，则只显示name 文件夹 -->
                <span v-if="!scope.row.type" v-html="highlightText(scope.row.name)"
                      style="cursor: pointer;"></span>
                <!-- 如果type不为空，则显示完整名称 文件+后缀 -->
                <span v-else v-html="highlightText(scope.row.name + '.' + scope.row.type)"
                      style="cursor: pointer;"></span>
              </div>
            </template>
            <!-- 重命名 -->
            <template v-else>
              <div style="display: flex; align-items: center;">
                <el-input class="rename-input" v-model="editedName"
                          @keyup.enter.native="saveRename(scope.row)"></el-input>
                <!-- 取消按钮 -->
                <el-button @click="saveRename(scope.row)" size="mini" type="primary" style="margin-left: 5px;">确定</el-button>
                <el-button @click="cancelRename(scope.row)" size="mini" type="danger" style="margin-left: 5px;">取消</el-button>
              </div>
            </template>
          </template>
        </el-table-column>

        <el-table-column label="">
          <template v-slot="scope">
            <template v-if="!scope.row.isEditing">
              <div>
                <div class="opt-container" v-if="scope.row.optShow">
                  <el-tooltip content="分享" effect="dark" :open-delay="100">
                    <i class="el-icon-share" style="margin-right: 10px; cursor: pointer"
                       @click="shareFile()"></i>
                  </el-tooltip>
                  <el-tooltip content="删除" effect="dark" :open-delay="100">
                    <i class="el-icon-delete-solid" style="margin-right: 10px; cursor: pointer"
                       @click="del(scope.row.id)"></i>
                  </el-tooltip>
                  <el-tooltip content="重命名" effect="dark" :open-delay="100">
                    <i class="el-icon-document" style="margin-right: 10px; cursor: pointer"
                       @click="rename(scope.row)"></i>
                  </el-tooltip>
                  <el-tooltip content="移动到" effect="dark" :open-delay="100">
                    <i class="el-icon-s-unfold" style="cursor: pointer"
                       @click="move(scope.row.id)"></i>
                  </el-tooltip>
                </div>
              </div>
            </template>
          </template>
        </el-table-column>
        <!--        <el-table-column prop="path" label="文件路径" show-overflow-tooltip></el-table-column>-->
        <!--        <el-table-column prop="type" label="文件类型"></el-table-column>-->
        <el-table-column prop="size" label="文件大小" :formatter="formatSize"
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']">
        </el-table-column>
        <!--        <el-table-column prop="createTime" :formatter="formatTime" label="创建时间"></el-table-column>-->
        <el-table-column prop="updateTime" :formatter="formatTime" label="修改时间"
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']">
          <!--                    <template slot-scope="scope">
                                <span v-if="scope.row.updateTime != null">
                                  {{ scope.row.updateTime | formatTime }}
                                </span>
                              </template>-->
        </el-table-column>
        <el-table-column prop="type" label="文件类型" :formatter="formatType" width="90"></el-table-column>
      </el-table>
    </div>
    <div id="contextmenu"
         v-show="menuVisible"
         class="menu">
      <div class="contextmenu__item"
           @click="handleFolderClick(CurrentRow)">打开
      </div>
      <!--      <div class="contextmenu__item"-->
      <!--           @click="download(CurrentRow)">下载(小文件)-->
      <!--      </div>-->
      <div class="contextmenu__item"
           @click="downloadByBrowser(CurrentRow)">下载
      </div>
      <div class="contextmenu__item"
           @click="shareFile()">分享
      </div>
      <div class="contextmenu__item"
           @click="rename(CurrentRow)">重命名
      </div>
      <div class="contextmenu__item"
           @click="copy(CurrentRow.id)">复制
      </div>
      <div class="contextmenu__item"
           @click="move(CurrentRow.id)">移动到
      </div>
      <div class="contextmenu__item"
           @click="details(CurrentRow)">详细信息
      </div>
<!--      <div class="divider">-->
<!--        <el-divider></el-divider>-->
<!--      </div>-->
      <hr>
      <div class="contextmenu__item"
           @click="del(CurrentRow.id)" style="color: #ff2723">删除
      </div>

    </div>

    <el-dialog title="分享" :visible.sync="shareDialogVisible" width="40%"
               center>
      <el-form ref="form" :model="shareForm" label-width="25%" :rules="rules">
        <el-form-item label="分享名称">
          <el-input v-model="shareForm.name" style="width:60%"></el-input>
        </el-form-item>
        <el-form-item label="过期时间" >
          <el-select v-model="shareForm.leftDays" style="width:60%">
            <el-option label="一天" value="1"></el-option>
            <el-option label="三天" value="3"></el-option>
            <el-option label="七天" value="7"></el-option>
            <!-- <el-option label="十四天" value="14"></el-option> -->
            <el-option label="三十天" value="30"></el-option>
            <el-option label="永久" value="-1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否需要密码">
          <el-radio-group v-model="shareForm.needCode">
            <el-radio label="0">无密码</el-radio>
            <el-radio label="1">有密码</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="shareForm.needCode === '1'" label="分享密码" prop="code">
          <el-input v-model="shareForm.code" placeholder="请输入分享密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button class="normal-button" @click="shareDialogVisible = false">取消</el-button>
          <el-button class="primary-button" type="primary" @click="onSubmit" :disabled="!isCodeValid">创建分享
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <template>
      <div class="dialog-files">
        <file-table-dialog :dialog-files-visible.sync="dialogFilesVisible" :src-id.sync="srcId" :type.sync="type"/>
        <!-- .sync 是 Vue.js 中的一种特殊语法，用于实现子组件和父组件之间双向绑定数据的功能。
        它可以简化父子组件之间的通信，特别是用于修改父组件中的 prop 数据。-->
      </div>
    </template>

    <template>
      <div class="uploader">
        <el-drawer
            title="上传文件"
            :visible.sync="uploaderVisible"
            :close-on-press-escape="false"
            size="50%"
            @close="uploaderClose"
        >
          <span><uploader :src-id.sync="uploaderSrcId"/></span>
        </el-drawer>

      </div>
    </template>

  </div>
</template>
<script>
import FileTableDialog from "@/views/front/fileTableDialog";
import Uploader from "@/views/front/uploader";
import FileIcon from "@/views/FileIcon";
import {downloadFile} from "@/App";
// 在需要使用的地方引入 Base64
import {Base64} from 'js-base64';

import {setItemWithExpiry} from "@/App"
import {getItemWithExpiry} from "@/App"
import {updateItemWithExpiry} from "@/App"

export default {
  name: "DiskFiles",
  components: {FileTableDialog, Uploader, FileIcon},
  data() {
    return {
      // extension: fileExtensions,

      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      name: null,
      user: getItemWithExpiry("user"),
      ids: [],

      requestCache: [], // 存储发送的请求 URL
      cacheIndex: -1,  // 缓存数组的当前索引
      maxCacheSize: 50, // 最大缓存大小

      isSearch: false,//当前是否处于查询模式
      searchText: "", // 搜索的关键字

      loading: true, // 控制加载动画的显示与隐藏
      folderId: -1, // 当前的文件夹的id
      pfoldrId: -1, // 文件夹指针

      path: "", // 当前路径

      menuVisible: false, // 右键菜单是否显示

      editedName: '',// 编辑文件的名称

      dialogFilesVisible: false, // 打开文件夹选择框的Dialog
      srcId: -1, // 移动的源文件夹id

      uploaderVisible: false, // 上传文件对话框是否显示
      uploaderSrcId: -1, // 上传文件的目标文件夹id

      shareDialogVisible: false, // 分享对话框是否显示

      shareForm: {
        name: '未命名',
        needCode: "0",
        code: '',
        leftDays: '7',
      },
      rules: {
        code: [
          {required: true, message: '请输入分享密码', trigger: 'blur'},
          {min: 4, max: 4, message: '密码长度必须为4个字符', trigger: 'blur'},
          {pattern: /^[0-9a-zA-Z]+$/, message: '密码只能包含英文字母或数字', trigger: 'blur'}
        ],
      },

      type: "move", // 移动类型，move 移动，copy 复制

      avatarSrc: 'https://cdn.cometuan.space/images/avatar/AIassistant.jpg',
      dialogAIVisible: false, // 对话框是否可见
      inputMessage: '', // 用户输入的消息
      messages: [], // 消息列表

      processingAI: false,
      placeholder: '请输入消息', // 输入框的placeholder
      inputMessages: ['介绍一下彗星网盘？','怎么进行文件秒传？','怎么进行文件分享？','不小心将文件删除了怎么办？','彗星网盘有什么功能？'],

      fileType: null, // 用户选择的文件类型
      tableData2: [], // 经过筛选的数据
      dataflag:0,

    }
  },
  mounted() {
  },
  created() {
    this.load(1)
    this.folderId = this.user.rootId
  },
  computed: {
    // 计算属性，过滤出属性 delete 为 false 的数据,data变化以后自动更新
    filteredData() {
      // console.log(this.folderId)
      // let data = this.tableData.filter(item => !item.delete)
      let data = this.tableData
      this.total = data.length
      this.getPath(this.folderId)
      return data;
    },
    // 计算属性，根据 isSearch 属性和 searchText 动态生成高亮文本
    highlightText() {
      // console.log(this.isSearch, this.searchText)
      return (name) => {
        if (this.isSearch && this.searchText) {
          // 使用正则表达式替换匹配的文本为带有样式的高亮文本
          return name.replace(new RegExp(this.searchText, 'gi'), match => {
            return `<span class="highlight">${match}</span>`;
          });
        } else {
          // 如果不是搜索状态，直接返回原始文本
          return name;
        }
      };
    },
    isCodeValid() {
      if (this.shareForm.needCode === '1') {
        // 根据需要密码的状态检查密码是否符合规则
        // 这里假设密码必须是4个字符的英文字母或数字
        const code = this.shareForm.code;
        return /^[a-zA-Z0-9]{4}$/.test(code);
      } else {
        // 不需要密码时，直接返回 true
        return true;
      }
    },
    processingAI() {
      return this.processingAI;
    },
    extensionToType() {
      return {
        zip: '压缩文件',
        rar: '压缩文件',
        '7z': '压缩文件',
        tar: '压缩文件',
        gz: '压缩文件',
        bz2: '压缩文件',
        jar: '压缩文件',
        mp4: '视频',
        avi: '视频',
        rmvb: '视频',
        wmv: '视频',
        flv: '视频',
        mp3: '音频',
        wav: '音频',
        wma: '音频',
        aac: '音频',
        flac: '音频',
        jpg: '图片',
        jpeg: '图片',
        png: '图片',
        gif: '图片',
        bmp: '图片',
        psd: '图片',
        webp: '图片',
        ico: '图片',
        txt: '文本文件',
        md: '文本文件',
        log: '文本文件',
        ini: '文本文件',
        xls: 'Excel 文件',
        xlsx: 'Excel 文件',
        csv: 'Excel 文件',
        pdf: 'PDF 文件',
        ppt: 'PPT 文件',
        pptx: 'PPT 文件',
        doc: 'Word 文件',
        docx: 'Word 文件',
        exe: '可执行文件',
        bat: '可执行文件',
        sh: '可执行文件',
        apk: '可执行文件',
        java: '代码文件',
        c: '代码文件',
        cpp: '代码文件',
        py: '代码文件',
        js: '代码文件',
        html: '代码文件',
        css: '代码文件',
        php: '代码文件',
        go: '代码文件',
        swift: '代码文件',
        scala: '代码文件',
        kotlin: '代码文件',
        sql: '代码文件',
        xml: '代码文件',
        json: '代码文件',
        vue: '代码文件',
        ts: '代码文件',
        yml: '代码文件',
        yaml: '代码文件',
      };
    },
    extensionToType2() {
      return {
        zip: 'Compressed File',
        rar: 'Compressed File',
        '7z': 'Compressed File',
        tar: 'Compressed File',
        gz: 'Compressed File',
        bz2: 'Compressed File',
        jar: 'Compressed File',
        mp4: 'Video',
        avi: 'Video',
        rmvb: 'Video',
        wmv: 'Video',
        flv: 'Video',
        mp3: 'Audio',
        wav: 'Audio',
        wma: 'Audio',
        aac: 'Audio',
        flac: 'Audio',
        jpg: 'Image',
        jpeg: 'Image',
        png: 'Image',
        gif: 'Image',
        bmp: 'Image',
        psd: 'Image',
        webp: 'Image',
        ico: 'Image',
        txt: 'Text File',
        md: 'Text File',
        log: 'Text File',
        ini: 'Text File',
        xls: 'Excel File',
        xlsx: 'Excel File',
        csv: 'Excel File',
        pdf: 'PDF File',
        ppt: 'PPT File',
        pptx: 'PPT File',
        doc: 'Word File',
        docx: 'Word File',
        exe: 'Executable File',
        bat: 'Executable File',
        sh: 'Executable File',
        apk: 'Executable File',
        java: 'Code File',
        c: 'Code File',
        cpp: 'Code File',
        py: 'Code File',
        js: 'Code File',
        html: 'Code File',
        css: 'Code File',
        php: 'Code File',
        go: 'Code File',
        swift: 'Code File',
        scala: 'Code File',
        kotlin: 'Code File',
        sql: 'Code File',
        xml: 'Code File',
        json: 'Code File',
        vue: 'Code File',
        ts: 'Code File',
        yml: 'Code File',
        yaml: 'Code File',
      };
    },
  },
  watch: {
    messages() {
      this.scrollToBottom();
    },
    dialogFilesVisible(newVal, oldVal) {
      if (oldVal && !newVal) {
        // 当 dialogFilesVisible 从 true 变为 false 时执行的逻辑
        // console.log("dialogFilesVisible 从 true 变为 false")
        // 刷新当前页面
        setTimeout(() => {
          location.reload();
        }, 200); // 延迟200毫秒
      }
    },
    dialogAIVisible(newValue, oldValue) {
      if (newValue === false) {
        // 当对话框关闭时执行的代码
        this.clearMessages(); // 清理消息列表
      }
    },
    processingAI(newValue) {
      if (newValue) {
        this.placeholder = 'AI助手正在思考';
      } else {
        this.placeholder = '请输入消息';
      }
    },
    fileType(newVal, oldVal) {
      this.filterDataByType();
    },
  },
  methods: {
    customSortMethod(a, b) {
      let table = this.$refs.table
      // 获取排序状态对象
      let sortState = table.store.states;
      // 获取当前的排序字段
      let currentSortProp = sortState.sortProp;
      // 获取当前的排序顺序
      let currentSortOrder = sortState.sortOrder;
      // console.log(currentSortProp, currentSortOrder)
      if (a.folder === b.folder) {
        // 如果两个值相等，按照要排序的字段排序
        if (currentSortProp === 'name') {
          return a.name.localeCompare(b.name);
        } else if (currentSortProp === 'updateTime') {
          return a.updateTime - b.updateTime
        } else if (currentSortProp === 'size') {
          return a.size - b.size
        }
      } else if (a.folder) {
        if (currentSortOrder === 'ascending') {
          return -1;
        } else if (currentSortOrder === 'descending') {
          return 1;
        }
      } else if (b.folder) {
        if (currentSortOrder === 'ascending') {
          return 1;
        } else if (currentSortOrder === 'descending') {
          return -1;
        }
      }
    },
    del(id) {   // 单个删除
      this.$confirm('确定要删除选中的文件吗？', '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/files/' + id).then(res => {
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
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
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
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.handleCacheAndGetFileRequest('/files')
      this.folderId = this.user.rootId
    },
    selectAll(pageNum) {
      this.folderId = this.user.rootId

      this.isSearch = true;
      this.searchText = this.name;
      //根据条件查询所有数据
      if (pageNum) this.pageNum = pageNum
      this.loading = true;
      this.requestCache = [];
      this.cacheIndex = 0;
      //如果搜素条件为空，则相当于重置
      if (this.name === null || this.name === "") {
        this.isSearch = false;
        this.reset()
        return;
      }
      // this.requestCache = this.requestCache.slice(0, this.cacheIndex + 1)
      this.$request.get("/files/all", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.loading = false;
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.tableData = res.data
        }
      })
    },
    getPath(id) {
      this.$request.get('/files/file/' + id).then(res => {
        if (res.code === '200') {
          this.path = res.data.path.replace(/\//g, '>'); // 使用正则表达式替换所有匹配到的 /
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
    },
    reset() {
      this.name = null
      this.isSearch = false;
      this.searchText = "";
      this.fileType=null;
      // this.load(1);
      this.folderId = this.user.rootId
      this.handleCacheAndGetFileRequest("/files")
    },
    reload() {
      this.getFileRequest(this.requestCache[this.cacheIndex])
    },
    //发送请求获取文件夹内的文件列表，并更新表格数据和文件路径。
    handleCacheAndGetFileRequest(url) {
      // 用于封装getFileRequest和addToCache方法，统一处理
      // 丢弃索引之后的缓存(因为用户已经重新点击了新的路径)
      this.requestCache = this.requestCache.slice(0, this.cacheIndex + 1)
      this.isSearch = false;
      this.addToCache(url)
      this.getFileRequest(url)
    },
    getFileRequest(url) {// 发送请求,获取数据。不要直接调用此方法，调用handleCacheAndGetFileRequest方法
      //前进和后退可以直接调用此方法。
      this.loading = true;
      this.$request.get(url, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        this.loading = false;
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.tableData = res.data
          this.total = res.data.length
        }
      })
    },
    addToCache(url) {
      // 将新的 URL 添加到缓存中
      this.requestCache.push(url)
      // 如果缓存大小超过限制，则丢弃最老的请求 URL
      if (this.requestCache.length > this.maxCacheSize) {
        this.requestCache.shift()
      }
      // 更新缓存索引
      this.cacheIndex = this.requestCache.length - 1
    },
    //处理文件夹图标的点击事件
    handleFolderClick(row) {
      if (row.folder) {
        this.handleCacheAndGetFileRequest('/files/folder/' + row.id)
        this.folderId = row.id
      } else {
        //let originUrl = 'http://localhost:12345/download?diskId=' + row.diskId + "&fileId=" + row.id;
        // console.log("total:"+row.size)
        if (row.size > 30 * 1024 * 1024) {
          this.$message.error("文件过大，暂不支持在线预览");
          return;
        }
        let originUrl = process.env.VUE_APP_BASEURL + '/download?diskId=' + row.diskId + "&fileId=" + row.id;
        // console.log(originUrl)
        originUrl = originUrl + '&fullfilename=' + row.name + "." + row.type;
        // window.open(process.env.VUE_APP_FILE_VIEW_URL+'?url=' + encodeURIComponent(Base64.encode(originUrl)));
        let url = process.env.VUE_APP_FILE_VIEW_URL + '?url=' + encodeURIComponent(Base64.encode(originUrl))
        console.log(url)
        window.open(url);
      }
    },
    backNavigation() {
      // 判断是否有可后退的请求
      if (this.cacheIndex > 0) {
        this.cacheIndex--
        let folderId = this.requestCache[this.cacheIndex].split('/')[3]
        if (!isNaN(folderId)) {
          this.folderId = folderId//如果是数字，则更新文件夹id
        } else {
          this.folderId = this.user.rootId//如果不是数字，则说明回到根目录
        }
        this.getFileRequest(this.requestCache[this.cacheIndex])

      }
    },
    forwardNavigation() {
      // 判断是否有可前进的请求
      if (this.cacheIndex < this.requestCache.length - 1) {
        this.cacheIndex++
        let folderId = this.requestCache[this.cacheIndex].split('/')[3]
        if (!isNaN(folderId)) {
          this.folderId = folderId//如果是数字，则更新文件夹id
        } else {
          this.folderId = this.user.rootId//如果不是数字，则说明回到根目录
        }
        this.getFileRequest(this.requestCache[this.cacheIndex])
      }
    },
    addFolder() {
      this.$request({
        url: '/files/folder',
        method: 'POST',
        data: {name: '新建文件夹', folderId: this.folderId}
      }).then(res => {
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.reload()
        }
      })
    },
    // 右键菜单
    rightClick(row, column, event) {
      // 判断当前行是否已经被选中
      //let isSelected = this.$refs.table.selection.includes(row);
      // 如果当前行未被选中，则添加到选中行列表中
      this.$refs.table.clearSelection();//清空选中行列表
      //if (!isSelected) {
      this.$refs.table.toggleRowSelection(row);
      //}
      this.testModeCode = row.testModeCode
      this.menuVisible = false // 先把模态框关死，目的是 第二次或者第n次右键鼠标的时候 它默认的是true
      this.menuVisible = true // 显示模态窗口，跳出自定义菜单栏
      event.preventDefault() //关闭浏览器右键默认事件
      this.CurrentRow = row
      var menu = document.querySelector('.menu')
      this.styleMenu(menu)
    },
    foo() {
      // 取消鼠标监听事件 菜单栏
      this.menuVisible = false
      document.removeEventListener('click', this.foo) // 关掉监听，
    },
    styleMenu(menu) {
      let clientX = event.clientX
      let clientY = event.clientY
      let menuWidth = menu.offsetWidth
      let menuHeight = menu.offsetHeight
      if (menuWidth === 0) {
        menuWidth = 150
      }
      if (menuHeight === 0) {
        menuHeight = 250
      }
      let windowWidth = window.innerWidth
      let windowHeight = window.innerHeight
      console.log(clientX, clientY, menuWidth, menuHeight, windowWidth, windowHeight)
      if (clientX + menuWidth + 10 > windowWidth) {
        menu.style.left = clientX - menuWidth - 2 + 'px';
      } else {
        menu.style.left = clientX + 2 + 'px';
      }
      document.addEventListener('click', this.foo);
      if (clientY + menuHeight + 10 > windowHeight) {
        menu.style.top = clientY - menuHeight - 2 + 'px';
      } else {
        menu.style.top = clientY + 2 + 'px';
      }
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
    // 重命名操作
    rename(row) {
      // 遍历数据源中的每一行，将其 isEditing 属性设为 false
      this.tableData.forEach(item => {
        this.$set(item, 'isEditing', false);
      });
      // 将当前行设置为正在编辑状态
      this.$set(row, 'isEditing', true);
      this.editedName = row.name + (row.type ? '.' + row.type : '');
    },
    // 保存重命名后的名称
    saveRename(row) {
      console.log(row.name, row.type)
      console.log(this.editedName)
      let newName = this.editedName;
      if (row.folder) {
        row.name = newName;
        row.type = "";
      } else {
        const lastIndex = this.editedName.lastIndexOf('.');
        let name = this.editedName.substring(0, lastIndex);
        let type = this.editedName.substring(lastIndex + 1);
        if (lastIndex === -1) {
          name = this.editedName;
          type = "";
        }
        row.name = name;
        row.type = type;
        newName = name + (type === "" ? "" : "." + type);
      }
      console.log("newName:" + newName)
      this.$request.put('/files/rename', {id: row.id, name: newName}).then(res => {
        if (res.code === '200') {
          this.$message.success('重命名成功')
          this.reload()
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
      this.$set(row, 'isEditing', false);
      this.reload();
    },
    // 取消重命名操作
    cancelRename(row) {
      if (row.isEditing) {
        // 取消编辑状态，恢复原始名称
        this.$set(row, 'isEditing', false);
        this.$message.info('取消重命名')
        this.reload();
      }
    },
    move(id) {
      this.srcId = id
      this.type = 'move'
      this.dialogFilesVisible = true
    },
    copy(id) {
      this.srcId = id
      this.type = 'copy'
      this.dialogFilesVisible = true
    },
    uploadFile() {
      this.uploaderVisible = true
      this.uploaderSrcId = parseInt(this.folderId)
    },
    uploaderClose() {
      this.reload()
    },
    mouseEnter(row) {
      this.$set(row, 'optShow', true)
    },
    mouseLeave(row) {
      this.$set(row, 'optShow', false)
    },
    async downloadByBrowser(row) {
      if (row.folder === true) {
        this.$message.info('暂不支持下载文件夹')
        return
      }
      console.log("下载文件："+row.name)
      downloadFile(row.diskId, row.id)
    },
    downloadBatch() {
      if (!this.ids.length) {
        this.$message.warning('请选择数据')
        return
      }
      console.log(this.ids)
      for (let i = 0; i < this.ids.length; i++) {
        let row = this.tableData.find(item => item.id === this.ids[i])

        // 添加延迟
        setTimeout(() => {
          this.downloadByBrowser(row)
        }, i * 1000) // 每个下载操作之间延迟1秒
      }
    },
    /*download(row) {
      if (row.folder === true) {
        this.$message.info('暂不支持下载文件夹')
        console.log("是文件夹：" + row.name)
        return
      }
      this.XHRLoadFile(process.env.VUE_APP_BASEURL + '/download/' + row.id + '/0', {})
    },

    //通过XMLHttpRequest发送post请求下载文件
    //url : 请求的Url
    //data ： 请求的数据
    XHRLoadFile: (url, data) => {
      let xhr = new XMLHttpRequest()
      xhr.open('post', url)
      //如果需要请求头中这是token信息可以在这设置
      // xhr.setRequestHeader('Content-Type','application/json;charset=UTF-8')
      let user = getItemWithExpiry("user")
      console.log(user.token)
      let token = user.token
      xhr.setRequestHeader('token', token)
      xhr.responseType = 'blob'
      xhr.send(JSON.stringify(data))
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
          const blob = new Blob([xhr.response])
          let url = window.URL.createObjectURL(blob)

          //创建一个a标签元素，设置下载属性，点击下载，最后移除该元素
          let link = document.createElement('a')
          link.href = url
          link.style.display = 'none'
          //取出下载文件名
          const disposition = xhr.getResponseHeader('content-disposition')
          let fileName = disposition.substring(disposition.indexOf("=") + 1);
          const downloadFileName = decodeURIComponent(fileName)
          link.setAttribute('download', downloadFileName)
          link.click()
          window.URL.revokeObjectURL(url)
        }
      }
    },*/
    shareFile() {
      if (!this.ids.length) {
        this.$message.warning('请选择数据')
        return
      }
      this.shareDialogVisible = true
    },
    onSubmit() {
      if (!this.ids.length) {
        this.$message.warning('请选择')
        return
      }
      console.log(this.ids)
      console.log(this.ids.join(','))
      this.$request.post('/share', {
        fileIds: this.ids.join(','),
        name: this.shareForm.name,
        code: this.shareForm.code,
        leftDays: this.shareForm.leftDays,
      }).then(res => {
        if (res.code === '200') {   // 表示操作成功
          this.$message.success('分享成功')
          // this.$alert('链接' + res.data.path, '分享成功', {
          //   confirmButtonText: '确定',
          // });
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
        this.shareDialogVisible = false
      })
    },
    details(row) {
      let message =
          "<div style='width:300px; margin: 0 auto; text-align: left;'>" +
          "<strong style='color:black'>文件名：</strong>" +
          "<span style='color:gray'>" + row.name + "</span><br>" +
          "<strong style='color:black'>文件类型：</strong>" +
          "<span style='color:gray'>" + (row.folder ? "文件夹" : row.type || "其它") + "</span><br>" +
          "<strong style='color:black'>文件大小：</strong>" +
          "<span style='color:gray'>" + this.$options.filters.sizeFormat(row.size, 3) + "</span><br>" +
          "<strong style='color:black'>创建时间：</strong>" +
          "<span style='color:gray'>" + this.$options.filters.formatTime(row.createTime) + "</span><br>" +
          "<strong style='color:black'>修改时间：</strong>" +
          "<span style='color:gray'>" + this.$options.filters.formatTime(row.updateTime) + "</span><br>" +
          "<strong style='color:black'>文件路径：</strong>" +
          "<span style='color:gray'>" + row.path + "</span>";

      this.$confirm(message, '详细信息', {
        showCancelButton: false,
        showConfirmButton: false,
        center: true,
        dangerouslyUseHTMLString: true // 使用 HTML 字符串
      }).then(() => {
      }).catch(() => {
      })
    },
    // 打开对话框
    showDialog() {
      this.dialogAIVisible = true
      this.clearMessages()
    },
    // 发送消息
    sendMessage() {
      if (this.inputMessage !== '') {
        this.messages.push(this.inputMessage) // 添加用户发送的消息到消息列表
        // 发送请求到 AI 接口
        this.sendAIRequest(this.inputMessage);
        this.inputMessage = '' // 清空输入框
      }else {
        this.$message.error('请输入消息');
      }
    },
    sendAIRequest(contents) {
      this.processingAI = true; // 设置为true，表示AI正在处理
      this.inputMessage = 'AI助手正在思考...';
          this.$request.post('http://localhost:12345/ai/chat', {
            data: {
              content: contents
            }
          })
          .then(res => {
            if (res.code === '200') {   // 假设返回的res.data包含code
              this.messages.push(res.data); // 添加 AI 回复的消息到消息列表
              this.processingAI = false; // AI处理完成，设置为false
            } else {
              this.$message.error('请求失败，未知原因');
              this.messages.push("请求失败请重试");
              this.processingAI = false; // AI处理完成，设置为false
            }
          })
          .catch(error => {
            // 网络错误处理
            this.$message.error('网络错误，请检查网络连接');
            this.messages.push("请求失败请重试");
            this.processingAI = false; // AI处理完成，设置为false
          });
    },
    clearMessages() {
      this.messages = []; // 清空消息列表
      this.inputMessage = ''; // 清空输入框
      this.messages.push("您好，有什么可以帮到您?")
    },
    // 滚动到底部的方法
    scrollToBottom() {
      // 使用 nextTick 确保在 DOM 更新后执行
      this.$nextTick(() => {
        const messageList = this.$refs.messageList;
        if (messageList) {
          // 将滚动条滚动到底部
          messageList.scrollTop = messageList.scrollHeight;
        }
      });
    },
    setMessages() {
      const randomIndex = Math.floor(Math.random() * this.inputMessages.length);
      this.inputMessage = this.inputMessages[randomIndex];
    },
    formatType(row) {
      if (row.type) {
        const typeDescription = this.extensionToType[row.type]; // 检查 row.type 是否有效
        return typeDescription || '未知'; // 如果没有找到对应的类型，返回 '未知'
      }
      return '文件夹'; // 当 row.type 为 undefined 时返回
    },
    handleFileTypeChange(fileType) {
      if (this.tableData2.length===0 && this.dataflag===0){
        //初始化tableData2
        const data = this.tableData
        this.tableData2=data
        this.dataflag=1
      }
      if (this.pfoldrId!==this.folderId){
        this.tableData2=this.tableData
        this.pfoldrId=this.folderId
      }
      if (fileType === 'ALL'){
        this.fileType="ALL";
      }else {
        this.fileType = fileType;
      }
    },
    filterDataByType() {
      //按用户的文件分类选择改变要渲染的数据集
      let data2=this.tableData2
      if (this.fileType!=="ALL") {
        let data = data2.filter(item => this.extensionToType2[item.type] === this.fileType)
        this.tableData=data
      } else {
        this.tableData=data2
      }
    },
  }
}
</script>

<style scoped>

.blank {
  height: 3%
}

.operation {
  /*position: absolute;*/
  margin-left: 20px;
}

.backAndForward {
  margin-left: 1.5%;
  width: 80%;
  display: flex;
  justify-content: flex-start; /* 靠左对齐 */
}


::v-deep .search-input .el-input__inner {
  /*width: 12rem;*/
  height: 3rem;
  background-color: #EBEEF5;
  text-align: center;
  border: 0 !important;
  outline: none;
  font-weight: bold;
  font-size: 1rem;
  border-radius: 1rem;
}

::v-deep .highlight {
  background-color: yellow;
  color: #0d53ff;
  font-weight: bold;
  font-size: 1rem;
}


::v-deep .rename-input .el-input__inner {
  overflow: visible;
  width: 100%;
  text-align: left;
  border: 0 !important;
  outline: none;
  font-size: 0.9rem;
  font-weight: bold;
}

.dialog-files {
  z-index: 999;
}

.name-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.opt-container {
  color: #606266;
  font-size: 1rem;
  font-weight: bold;
}

.divider {
  height: 0;
}

.navigation-button {
  border: 0.2rem solid;
}
</style>
