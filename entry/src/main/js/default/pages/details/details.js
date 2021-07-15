import prompt from '@system.prompt';
import router from '@system.router';
const ABILITY_TYPE_EXTERNAL = 0;
const ACTION_SYNC = 0;
const ACTION_MESSAGE_CODE_INSERT = 1002;
const ACTION_MESSAGE_CODE_UPDATE = 1004;
export default{
    data:{
        newTitle:'',
        newDescription:'',
        newItem:{"title":'',"description":''},
        buttonInfo:''
    },
    onInit(){
        console.log(this.index)
        if(this.index != undefined){
            this.newTitle = this.$app.$def.data.textList[this.index].title
            this.newDescription = this.$app.$def.data.textList[this.index].description
            this.buttonInfo = "确认更改"
        }
        else{
            this.buttonInfo = "确认添加"
        }
    },
    getNewTitle(e){
        this.newTitle = e.value
    },
    getNewDescription(e) {
        this.newDescription = e.value
    },
    async addItem(){
        //Param for linking data ability
        var actionData = {};
        actionData.idx = this.index
        actionData.title = this.newTitle
        actionData.description = this.newDescription
        var action = {};
        action.bundleName = 'com.example.todo';
        action.abilityName = 'ServiceAbility';
        action.abilityType = ABILITY_TYPE_EXTERNAL;
        action.data = actionData;
        action.syncOption = ACTION_SYNC;
        if(this.index != undefined) {
            this.$app.$def.data.textList[this.index].title = this.newTitle
            this.$app.$def.data.textList[this.index].description = this.newDescription
            action.messageCode = ACTION_MESSAGE_CODE_UPDATE;
        }
        else {
            this.newItem.title = this.newTitle
            this.newItem.description = this.newDescription
            action.messageCode = ACTION_MESSAGE_CODE_INSERT;
        }
        console.info("Insert/Updating Calling PA...");
        var result = await FeatureAbility.callAbility(action);
        var ret = JSON.parse(result);
        console.info("Insert/Updating Return from PA");
        if (ret.code == 0) {
            console.info('Insert/Update result is:' + JSON.stringify(ret.abilityResult));
            prompt.showToast({
                message: "操作成功",
                duration: 3000
            })
        } else {
            console.error('insert/update error code:' + JSON.stringify(ret.code));
        }
        router.back()
    },
    return2Index(){
        router.back()
    }
}