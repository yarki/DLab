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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalModule } from './../modal/index';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ComputationalResourceCreateDialog } from './computational-resource-create-dialog.component';
import { DropdownListModule } from './../form-controls/dropdown-list/index';

export * from './computational-resource-create-dialog.component';

@NgModule({
  imports: [CommonModule, ModalModule, FormsModule, ReactiveFormsModule, DropdownListModule],
  declarations: [ComputationalResourceCreateDialog],
  exports: [ComputationalResourceCreateDialog]
})

export class ComputationalResourceCreateDialogModule { }
