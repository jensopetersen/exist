/*
	Copyright (c) 2004-2009, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/


if(!dojo._hasResource["dojox.off._common"]){dojo._hasResource["dojox.off._common"]=true;dojo.provide("dojox.off._common");dojo.require("dojo.gears");dojo.require("dojox.storage");dojo.require("dojox.sql");dojo.require("dojox.off.sync");dojo.mixin(dojox.off,{isOnline:false,NET_CHECK:5,STORAGE_NAMESPACE:"_dot",enabled:true,availabilityURL:dojo.moduleUrl("dojox","off/network_check.txt"),goingOnline:false,coreOpFailed:false,doNetChecking:true,hasOfflineCache:null,browserRestart:false,_STORAGE_APP_NAME:window.location.href.replace(/[^0-9A-Za-z_]/g,"_"),_initializeCalled:false,_storageLoaded:false,_pageLoaded:false,onLoad:function $DAgD_(){},onNetwork:function $DAgE_(_1){},initialize:function $DAgF_(){this._initializeCalled=true;if(this._storageLoaded&&this._pageLoaded){this._onLoad();}},goOffline:function $DAgG_(){if((dojox.off.sync.isSyncing)||(this.goingOnline)){return;}this.goingOnline=false;this.isOnline=false;},goOnline:function $DAgH_(_2){if(dojox.off.sync.isSyncing||dojox.off.goingOnline){return;}this.goingOnline=true;this.isOnline=false;this._isSiteAvailable(_2);},onFrameworkEvent:function $DAgI_(_3,_4){if(_3=="save"){if(_4.isCoreSave&&(_4.status==dojox.storage.FAILED)){dojox.off.coreOpFailed=true;dojox.off.enabled=false;dojox.off.onFrameworkEvent("coreOperationFailed");}}else{if(_3=="coreOperationFailed"){dojox.off.coreOpFailed=true;dojox.off.enabled=false;}}},_checkOfflineCacheAvailable:function $DAgJ_(_5){this.hasOfflineCache=dojo.gears.available;_5();},_onLoad:function $DAgK_(){dojox.off.files.cache(dojo.moduleUrl("dojo","dojo.js"));this._cacheDojoResources();dojox.off.files.cache(dojox.storage.manager.getResourceList());dojox.off.files._slurp();this._checkOfflineCacheAvailable(dojo.hitch(this,"_onOfflineCacheChecked"));},_onOfflineCacheChecked:function $DAgL_(){if(this.hasOfflineCache&&this.enabled){this._load(dojo.hitch(this,"_finishStartingUp"));}else{if(this.hasOfflineCache&&!this.enabled){this._finishStartingUp();}else{this._keepCheckingUntilInstalled();}}},_keepCheckingUntilInstalled:function $DAgM_(){this._finishStartingUp();},_finishStartingUp:function $DAgN_(){if(!this.hasOfflineCache){this.onLoad();}else{if(this.enabled){this._startNetworkThread();this.goOnline(dojo.hitch(this,function(){dojox.off.onLoad();}));}else{if(this.coreOpFailed){this.onFrameworkEvent("coreOperationFailed");}else{this.onLoad();}}}},_onPageLoad:function $DAgO_(){this._pageLoaded=true;if(this._storageLoaded&&this._initializeCalled){this._onLoad();}},_onStorageLoad:function $DAgP_(){this._storageLoaded=true;if(!dojox.storage.manager.isAvailable()&&dojox.storage.manager.isInitialized()){this.coreOpFailed=true;this.enabled=false;}if(this._pageLoaded&&this._initializeCalled){this._onLoad();}},_isSiteAvailable:function $DAgQ_(_6){dojo.xhrGet({url:this._getAvailabilityURL(),handleAs:"text",timeout:this.NET_CHECK*1000,error:dojo.hitch(this,function(_7){this.goingOnline=false;this.isOnline=false;if(_6){_6(false);}}),load:dojo.hitch(this,function(_8){this.goingOnline=false;this.isOnline=true;if(_6){_6(true);}else{this.onNetwork("online");}})});},_startNetworkThread:function $DAgR_(){if(!this.doNetChecking){return;}window.setInterval(dojo.hitch(this,function(){var d=dojo.xhrGet({url:this._getAvailabilityURL(),handleAs:"text",timeout:this.NET_CHECK*1000,error:dojo.hitch(this,function(_a){if(this.isOnline){this.isOnline=false;try{if(typeof d.ioArgs.xhr.abort=="function"){d.ioArgs.xhr.abort();}}catch(e){}dojox.off.sync.isSyncing=false;this.onNetwork("offline");}}),load:dojo.hitch(this,function(_b){if(!this.isOnline){this.isOnline=true;this.onNetwork("online");}})});}),this.NET_CHECK*1000);},_getAvailabilityURL:function $DAgS_(){var _c=this.availabilityURL.toString();if(_c.indexOf("?")==-1){_c+="?";}else{_c+="&";}_c+="browserbust="+new Date().getTime();return _c;},_onOfflineCacheInstalled:function $DAgT_(){this.onFrameworkEvent("offlineCacheInstalled");},_cacheDojoResources:function $DAgU_(){var _d=true;dojo.forEach(dojo.query("script"),function(i){var _f=i.getAttribute("src");if(!_f){return;}if(_f.indexOf("_base/_loader/bootstrap.js")!=-1){_d=false;}});if(!_d){dojox.off.files.cache(dojo.moduleUrl("dojo","_base.js").uri);dojox.off.files.cache(dojo.moduleUrl("dojo","_base/_loader/loader.js").uri);dojox.off.files.cache(dojo.moduleUrl("dojo","_base/_loader/bootstrap.js").uri);dojox.off.files.cache(dojo.moduleUrl("dojo","_base/_loader/hostenv_browser.js").uri);}for(var i=0;i<dojo._loadedUrls.length;i++){dojox.off.files.cache(dojo._loadedUrls[i]);}},_save:function $DAgV_(){},_load:function $DAgW_(_11){dojox.off.sync._load(_11);}});dojox.storage.manager.addOnLoad(dojo.hitch(dojox.off,"_onStorageLoad"));dojo.addOnLoad(dojox.off,"_onPageLoad");}