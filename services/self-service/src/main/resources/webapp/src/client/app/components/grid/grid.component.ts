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

import { Component, EventEmitter, Input, Output, ViewChild, OnInit } from "@angular/core";
import { UserResourceService } from "./../../services/userResource.service";
import { GridRowModel } from './grid.model';
import { CreateEmrModel } from "./createEmrModel";

@Component({
  moduleId: module.id,
  selector: 'ng-grid',
  templateUrl: 'grid.component.html',
  styleUrls: ['./grid.component.css']
})

export class Grid implements OnInit {

  isFilled: boolean = false;
  list: any;
  environments: Array<GridRowModel>;
  notebookName: any;

  model = new CreateEmrModel('', '');
  namePattern = "/S+";

  @ViewChild('createEmrModal') createEmrModal;
  @ViewChild('confirmationDialog') confirmationDialog;
  @ViewChild('detailDialog') detailDialog;
  @Input() emrTempls;
  @Input() shapes;


  constructor(
    private userResourceService: UserResourceService
    ) { }

  ngOnInit() {
    this.buildGrid();
  }

  buildGrid() {
    this.userResourceService.getUserProvisionedResources().subscribe((list) => {
      this.list = list;
      this.environments = this.loadEnvironments();
      console.log('models ', this.environments);
    });
  }

  containsNotebook(notebook_name):boolean {

    if(notebook_name)
      for (var index = 0; index < this.environments.length; index++)
        if(notebook_name.toLowerCase() ==  this.environments[index].name.toString().toLowerCase())
          return true;

        return false;
  }

  loadEnvironments(): Array<any> {
     if (this.list) {
       return this.list.map((value) => {
         return new GridRowModel(value.exploratory_name,
           value.status,
           value.shape,
           value.computational_resources);
       });
     }
   }

  printDetailEnvironmentModal(data) {
    this.detailDialog.open({ isFooter: false }, data);
  }

  mathAction(data, action) {
    console.log('action ' + action, data);
    if (action === 'deploy') {
      this.notebookName = data.name
      this.createEmrModal.open({ isFooter: false });
    } else if (action === 'run') {
      this.userResourceService
        .runExploratoryEnvironment({notebook_instance_name: data.name})
        .subscribe((result) => {
          console.log('startUsernotebook result: ', result);
          this.buildGrid();
        });
    } else if (action === 'stop') {
      this.confirmationDialog.open({ isFooter: false }, data, 'stop');
    } else if (action === 'terminate') {
      this.confirmationDialog.open({ isFooter: false }, data, 'terminate');
    }
  }

  createEmr(name, count, shape_master, shape_slave, tmplIndex){

    this.userResourceService
      .createComputationalResource({
        name: name,
        emr_instance_count: count,
        emr_master_instance_type: shape_master,
        emr_slave_instance_type: shape_slave,
        emr_version: this.emrTempls[tmplIndex].version,
        notebook_name: this.notebookName
      })
      .subscribe((result) => {
        console.log('result: ', result);

        if (this.createEmrModal.isOpened) {
         this.createEmrModal.close();
       }
       this.buildGrid();
      });
      return false;
  };

  validate(name, count) {
    this.isFilled = (name.value.length > 1) && (+count.value > 0);
  }
}
