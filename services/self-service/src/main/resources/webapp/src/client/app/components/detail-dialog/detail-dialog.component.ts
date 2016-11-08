/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

 import { Component, OnInit, ViewChild, Input } from '@angular/core';
 import { Modal } from './../modal/modal.component';

 @Component({
   moduleId: module.id,
   selector: 'detail-dialog',
   templateUrl: 'detail-dialog.component.html'
 })

 export class DetailDialog {
 	notebook: any;
 	upTime: any;

 	
 	@ViewChild('bindDialog') bindDialog;

 	open(param, notebook) {
     this.bindDialog.open(param);
     this.upTime = this.diffBetweenDates(notebook.time);
     this.notebook = notebook;
   }

   diffBetweenDates(date){
        let dateArr = date.split(" ")[0].split('/');
        let timeArr = date.split(" ")[1].split(':');
        let createdDate = new Date(20 + dateArr[2], dateArr[1] - 1, dateArr[0], timeArr[0], timeArr[1]);
        let endTime = new Date();
        let difference = endTime.getTime() - createdDate.getTime();
        let days = Math.round(difference / (60000*60));
        return days;
    }
 }

