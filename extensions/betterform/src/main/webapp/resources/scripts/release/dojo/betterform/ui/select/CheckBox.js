/*
	Copyright (c) 2004-2009, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/


if(!dojo._hasResource["betterform.ui.select.CheckBox"]){dojo._hasResource["betterform.ui.select.CheckBox"]=true;dojo.provide("betterform.ui.select.CheckBox");dojo.require("dijit.form.FilteringSelect");dojo.require("betterform.ui.ControlValue");dojo.require("dijit.form.CheckBox");dojo.declare("betterform.ui.select.CheckBox",[betterform.ui.ControlValue,dijit.form.CheckBox],{selectWidgetId:"",value:"",selectWidget:null,templateString:"<span class=\"dijitReset dijitInline\" waiRole=\"presentation\"\n\t><input\n\t \ttype=\"${type}\" name=\"${name}\"\n\t\tclass=\"dijitReset dijitCheckBoxInput\"\n\t\tdojoAttachPoint=\"focusNode\"\n\t \tdojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse,onclick:_onClick\"\n/></span>\n",postMixInProperties:function $DA3v_(){this.inherited(arguments);this.selectWidget=dijit.byId(this.selectWidgetId);if(this.srcNodeRef!=undefined){this.currentValue=dojo.attr(this.srcNodeRef,"value");}},onClick:function $DA3w_(_1){this.inherited(arguments);if(this.selectWidget==undefined){this.selectWidget=dijit.byId(this.selectWidgetId);}if(this.selectWidget==undefined){conosle.warn("CheckBox.onClick: Select (CheckBoxGroup) "+this.selectWidgetId+" could not be found");return;}this.selectWidget._setCheckBoxGroupValue();},getControlValue:function $DA3x_(){return this.currentValue;}});}