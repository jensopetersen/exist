/*
	Copyright (c) 2004-2009, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/


if(!dojo._hasResource["betterform.XFormsModelElement"]){dojo._hasResource["betterform.XFormsModelElement"]=true;dojo.provide("betterform.XFormsModelElement");dojo.require("dijit._Widget");dojo.require("betterform.XFormsModelElement");dojo.declare("betterform.XFormsModelElement",dijit._Widget,{constructor:function $DA6Y_(){},postCreate:function $DA6Z_(){},getInstanceDocument:function $DA6a_(_1,_2){this._useLoadingMessage();dwr.engine.setErrorHandler(fluxProcessor._handleExceptions);XFormsModelElement.getInstanceDocument(this.id,_1,fluxProcessor.sessionKey,_2);},getInstanceAsString:function $DA6b_(_3,_4){this._useLoadingMessage();dwr.engine.setErrorHandler(fluxProcessor._handleExceptions);XFormsModelElement.getInstanceAsString(this.id,_3,fluxProcessor.sessionKey,_4);},rebuild:function $DA6c_(){this._useLoadingMessage();dwr.engine.setErrorHandler(fluxProcessor._handleExceptions);XFormsModelElement.rebuild(this.id,fluxProcessor.getSessionKey(),null);},recalculate:function $DA6d_(){this._useLoadingMessage();dwr.engine.setErrorHandler(fluxProcessor._handleExceptions);XFormsModelElement.recalculate(this.id,fluxProcessor.getSessionKey(),null);},revalidate:function $DA6e_(){this._useLoadingMessage();dwr.engine.setErrorHandler(fluxProcessor._handleExceptions);XFormsModelElement.revalidate(this.id,fluxProcessor.getSessionKey(),null);},refresh:function $DA6f_(){this._useLoadingMessage();dwr.engine.setErrorHandler(fluxProcessor._handleExceptions);XFormsModelElement.refresh(this.id,fluxProcessor.getSessionKey(),null);},_useLoadingMessage:function $DA6g_(){dwr.engine.setPreHook(function(){document.getElementById("indicator").className="xfEnabled";});dwr.engine.setPostHook(function(){document.getElementById("indicator").className="xfDisabled";});}});}