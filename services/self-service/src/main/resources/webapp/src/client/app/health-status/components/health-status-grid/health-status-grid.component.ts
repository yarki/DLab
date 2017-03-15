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
import { HealthStatusService } from '../../../services/healthStatus.service';
import { EnvironmentStatusModel } from '../../environment-status.model';
import { ConfirmationDialogType } from '../../../components/confirmation-dialog/confirmation-dialog-type.enum';

@Component({
  moduleId: module.id,
  selector: 'health-status-grid',
  templateUrl: 'health-status-grid.component.html',
  styleUrls: ['./health-status-grid.component.css',
              '../../../components/resources-grid/resources-grid.component.css']
})
export class HealthStatusGridComponent {

   @Input() environmentsHealthStatuses: Array<EnvironmentStatusModel>;
   @Output() refreshGrid: EventEmitter<{}> = new EventEmitter();

   @ViewChild('confirmationDialog') confirmationDialog;

    constructor(
      private healthStatusService: HealthStatusService
    ) { }

    ngOnInit(): void {
      this.buildGrid();
    }

    buildGrid(): void {
      this.refreshGrid.emit();
    }

    healthStatusAction(data, action: string) {
      if (action === 'run') {
        this.healthStatusService
          .runEdgeNode()
          .subscribe(() => this.buildGrid());
      } else if (action === 'stop') {
        this.confirmationDialog.open({ isFooter: false }, data, ConfirmationDialogType.StopEdgeNode);
      } else if (action === 'recreate') {
        this.healthStatusService
          .recreateEdgeNode()
          .subscribe(() => this.buildGrid());
      }
  }
}
