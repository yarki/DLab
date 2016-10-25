/******************************************************************************************************

Copyright (c) 2016 EPAM Systems Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*****************************************************************************************************/

import { Component, EventEmitter, Input, Output, OnInit } from "@angular/core";
import { EnvironmentsService } from './../../services/environments.service';
import { UserResourceService } from "./../../services/userResource.service";
import { GridRowModel } from './grid.model';

@Component({
  moduleId: module.id,
  selector: 'ng-grid',
  templateUrl: 'grid.component.html',
  styleUrls: ['./grid.component.css']
})

export class Grid implements OnInit {

  list: any;
  environments: Array<GridRowModel>;

  constructor(
    private environmentsService: EnvironmentsService,
    private userResourceService: UserResourceService,
  ) { }

  ngOnInit() {
    this.buildGrid();
  }

  buildGrid() {
    // this.environmentsService.getEnvironmentsList().subscribe((list) => {
    this.userResourceService.getGridData().subscribe((list) => {

      this.list = list;
      this.environments = this.loadEnvironments();
      console.log('models ', this.environments);
    });
  }

  loadEnvironments(): Array<any> {
    if (this.list['RESOURCES']) {
      return this.list['RESOURCES'].map((value) => {
        return new GridRowModel(value.ENVIRONMENT_NAME,
          value.STATUS,
          value.SHAPE,
          value.COMPUTATIONAL_RESOURCES);
      });
    }
  }

  printDetailEnvironmentModal(data) {
    console.log(data);
  }

  mathAction(data, action) {
    console.log('action ' + action, data);
  }
}
