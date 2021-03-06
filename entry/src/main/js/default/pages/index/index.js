import router from '@system.router';
import prompt from '@system.prompt';

const ABILITY_TYPE_EXTERNAL = 0;
const ACTION_SYNC = 0;
const ACTION_MESSAGE_CODE_SELECT = 1001;
const ACTION_MESSAGE_CODE_DELETE = 1003;
const ACTION_MESSAGE_CODE_CONTINUE = 1005;
export default {
    data: {
        inputValue:'',
        headTitle: 'TODO List',
        textList:  [],
        continue: false
    },
    onInit() {
        console.log("onInit")
        this.textList = []
    },
    select:async function() {
        var action = {};
        action.bundleName = 'com.example.todo';
        action.abilityName = 'ServiceAbility';
        action.messageCode = ACTION_MESSAGE_CODE_SELECT;
        action.abilityType = ABILITY_TYPE_EXTERNAL;
        action.syncOption = ACTION_SYNC;
        console.info("Calling PA...");
        var result = await FeatureAbility.callAbility(action);
        return result;
    },
    onShow(){
        this.select().then(res => {
            var ret = JSON.parse(res);
            console.info("Return from PA");
            if (ret.code == 0) {
                console.info('Original select result is:' + JSON.stringify(this.textList));
                console.info('select result is:' + JSON.stringify(ret.abilityResult));
                if(this.textList.length === 0) {
                    this.textList = [...ret.abilityResult]
                }
                else {
                    this.textList = [...this.textList, ...ret.abilityResult]
                    console.info("after select "+this.textList)
                }
            } else {
                if(this.textList.length === 0) {
                    this.textList = [{title: 'JS FA'}]
                }
                console.error('select error code:' + JSON.stringify(ret.code));
            }
            this.$app.$def.data.textList = [...this.textList]
        });
    },
    getListItem(e){
        this.inputValue = e.value
    },
    addInput2List(item){
        this.textList.push(item)
    },
    async deleteItemFromList(index){
        var actionData = {};
        actionData.idx = this.index
        var action = {};
        action.bundleName = 'com.example.todo';
        action.abilityName = 'ServiceAbility';
        action.messageCode = ACTION_MESSAGE_CODE_DELETE;
        action.abilityType = ABILITY_TYPE_EXTERNAL;
        action.data = actionData;
        action.syncOption = ACTION_SYNC;
        console.info("DELETE Calling PA...");
        var result = await FeatureAbility.callAbility(action);
        var ret = JSON.parse(result);
        console.info("DELETE Return from PA");
        if (ret.code == 0) {
            prompt.showToast({
                message: "????????????",
                duration: 3000
            })
            this.textList.splice(index, 1);
            console.info('delete result is:' + JSON.stringify(ret.abilityResult));
        } else {
            prompt.showToast({
                message: "????????????",
                duration: 3000
            })
            console.error('delete error code:' + JSON.stringify(ret.code));
        }
    },
    addItem2List(){
        router.push ({
            uri:'pages/details/details', // ????????????????????????
            params: {
                length: this.textList.length
            }
        })
    },
    editItem(index){
        console.log(index);
        router.push ({
            uri:'pages/details/details', // ????????????????????????
            params: {
                index
            }
        })
    },
    tryContinueAbility: async function() {
        // ??????????????????
        let result = await FeatureAbility.continueAbility();
        console.info("result:" + JSON.stringify(result));
    },
    onStartContinuation() {
        // ??????????????????????????????????????????
        console.info("onStartContinuation");
        return true;
    },
    onCompleteContinuation(code) {
        // ?????????????????????code????????????
        console.info("nCompleteContinuation: code = " + code);
    },
    onSaveData(saveData) {
        // ???????????????savedData??????????????????
        var data = this.textList;
        console.info("onSaveData")
        Object.assign(saveData, data)
        console.info("endsaveData")
    },
    onRestoreData(restoreData) {
        // ??????????????????????????????
        console.info("onRestore")
        for(let i in restoreData){
            this.textList.push(restoreData[i])
            this.continue = true
        }
    },
}
