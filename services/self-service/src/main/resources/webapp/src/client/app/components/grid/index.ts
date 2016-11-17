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

import {NgModule, Component} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {Grid} from "./grid.component";
import { ResourcesModule } from './../list-of-resources/index';
import { ModalModule } from './../modal/index';
import { ConfirmationDialogModule } from './../confirmation-dialog/index';
import { DetailDialogModule } from './../detail-dialog/index';

export * from "./grid.component";

@NgModule({
  imports: [CommonModule, ResourcesModule, ModalModule, ConfirmationDialogModule, DetailDialogModule, FormsModule],
  declarations: [Grid],
  exports: [Grid]
})

export class GridModule { }
