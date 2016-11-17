/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

 import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from '@angular/core';
 import { UserResourceService } from "./../../services/userResource.service";
 import { Modal } from './../modal/modal.component';

 @Component({
   moduleId: module.id,
   selector: 'confirmation-dialog',
   templateUrl: 'confirmation-dialog.component.html'
 })

 export class confirmationDialog {
   notebook: any;
   stopText:string = `Notebook server will be stopped and all connected EMR instances will be terminated.`;
   terminateText:string = `Notebook server and all connected EMR instances will be terminated.`;
   action:string;


   @ViewChild('bindDialog') bindDialog;
   @Output() buildGrid: EventEmitter<{}> = new EventEmitter();


   constructor(
    private userResourceService: UserResourceService
    ) { }

   open(param, notebook, action) {
     this.bindDialog.open(param);
     this.notebook = notebook;
     this.action = action;
   }
   close() {
     this.bindDialog.close();
   }

   stop() {
     let url = "/" + this.notebook.name + "/stop";

     this.userResourceService
        .suspendExploratoryEnvironment(url)
        .subscribe((result) => {
          console.log('stopUsernotebook result: ', result);

          this.close();
          this.buildGrid.emit();
        });
   }

   terminate(){
     let url = "/" + this.notebook.name + "/terminate";

     this.userResourceService
        .suspendExploratoryEnvironment(url)
        .subscribe((result) => {
          console.log('terminateUsernotebook result: ', result);

          this.close();
          this.buildGrid.emit();
        });
   }
 }
