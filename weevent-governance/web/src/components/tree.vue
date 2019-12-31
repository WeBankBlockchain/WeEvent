<template>
  <div id='tree'>
    <div v-for="(item, index) in treeData" :key='index' class='tree' >
      <div class='tree_content' :value='JSON.stringify(item)' :nopdeIndex='nodeIndex + index'>
        <el-select v-model="item.connectionOperator" v-show='nodeIndex + index !== "00"' size='small' placeholder="Connect">
          <el-option label="and" value="and"></el-option>
          <el-option label="or" value="or"></el-option>
        </el-select>
        <span class='line' v-show='nodeIndex + index !== "00"'>-</span>
        <el-select v-model="item.functionType" size='small' placeholder="Function" v-show='item.functionType !== "now" && item.functionType !== "currentDate" && item.functionType !== "currentTime"' @change='changeItem(item)'>
          <el-option-group
            v-for="group in options"
            :key="group.label"
            :label="group.label">
            <el-option
              v-for="item in group.options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-option-group>
        </el-select>
        <span class='line' v-show='item.functionType !== "now" && item.functionType !== "currentDate" && item.functionType !== "currentTime"'>-</span>
        <el-select v-model="item.columnName" size='small' placeholder='Key'>
          <el-option :label="key" :value="key" v-for='(item, key, index) in columnList' :key='index'></el-option>
        </el-select>
        <span class='line' v-show="item.functionType !== 'substring' && item.functionType !== 'concat'">-</span>
        <span class='line' v-show="item.functionType === 'substring' || item.functionType === 'concat'">(</span>
        <el-input size='small' placeholder="index" v-show="item.functionType === 'substring'" v-model.trim="item.columnMark" autocomplete="off"></el-input>
        <el-select v-model="item.columnMark" size='small' placeholder='Key' v-show="item.functionType === 'concat'">
          <el-option :label="key" :value="key" v-for='(item, key, index) in columnList' :key='index'></el-option>
        </el-select>
        <span class='line' v-show="item.functionType === 'substring' || item.functionType === 'concat'">)</span>
        <el-select v-model="item.conditionalOperator" size='small' placeholder='Relation'>
          <el-option label=">" value=">"></el-option>
          <el-option label=">=" value=">="></el-option>
          <el-option label="<" value="<"></el-option>
          <el-option label="<=" value="<="></el-option>
          <el-option label="!=" value="!="></el-option>
          <el-option label="==" value="=="></el-option>
        </el-select>
        <span class='line'>-</span>
        <el-input size='small' placeholder='Value' v-model.trim="item.sqlCondition" autocomplete="off" v-show='item.functionType !== "now" && item.functionType !== "currentDate" && item.functionType !== "currentTime"'></el-input>
        <el-select v-model="item.functionType" size='small' placeholder="Function" v-show='item.functionType === "now" || item.functionType === "currentDate" || item.functionType === "currentTime"'>
          <el-option-group
            v-for="group in options"
            :key="group.label"
            :label="group.label">
            <el-option
              v-for="item in group.options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-option-group>
        </el-select>
        <span class='item_option el-icon-circle-plus' @click='addItem(nodeIndex + "-" + index)'></span>
        <span class='item_option el-icon-delete-solid' @click='delItem(nodeIndex + "-" + index)'></span>
        <p class='warning'>错误提醒</p>
      </div>
      <tree :treeData="item.children" :nodeIndex='nodeIndex + "-" + index' :columnList='columnList'></tree>
    </div>
  </div>
</template>
<script>
export default {
  name: 'tree',
  props: {
    treeData: Array,
    nodeIndex: String,
    columnList: Object
  },
  data () {
    return {
      options: [
        {
          label: 'Time',
          options: [
            {
              value: 'now',
              label: 'now'
            },
            {
              value: 'currentDate',
              label: 'currentDate'
            },
            {
              value: 'currentTime',
              label: 'currentTime'
            }
          ]
        },
        {
          label: 'Number',
          options: [
            {
              value: 'abs',
              label: 'abs'
            },
            {
              value: 'ceil',
              label: 'ceil'
            },
            {
              value: 'floor',
              label: 'floor'
            },
            {
              value: 'round',
              label: 'round'
            }
          ]
        },
        {
          label: 'String',
          options: [
            {
              value: 'substring',
              label: 'substring'
            },
            {
              value: 'concat',
              label: 'concat'
            },
            {
              value: 'trim',
              label: 'trim'
            },
            {
              value: 'lcase',
              label: 'lcase'
            }
          ]
        }
      ]
    }
  },
  methods: {
    addItem (e) {
      let item = {
        'connectionOperator': '',
        'columnName': '',
        'conditionalOperator': '',
        'sqlCondition': '',
        'functionType': '',
        'columnMark': '',
        'children': []
      }
      let index = e.split('-').pop()
      this.treeData[index].children.push(item)
    },
    delItem (e) {
      let index = e.split('-').pop()
      this.treeData.splice(index, 1)
    },
    changeItem (e) {
      if (e.functionType === 'now' || e.functionType === 'currentDate' || e.functionType === 'currentTime') {
        e.sqlCondition = e.functionType
      }
      if (e.functionType !== 'substring' || e.functionType !== 'concat') {
        e.columnMark = ''
      }
    }
  },
  mounted () {}
}
</script>
