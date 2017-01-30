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

import { Routes } from '@angular/router';
import { AuthorizationGuard } from './security/authorization.guard';

export const routes: Routes = [{
    path: '',
    redirectTo: 'resources_list',
    pathMatch: 'full'
  }, {
    path: 'resources_list',
    loadChildren: 'app/home/home.module#HomeModule',
    canActivate: [AuthorizationGuard]
  }, {
    path: 'login',
    loadChildren: 'app/login/login.module#LoginModule'
  }, {
    path: 'help/accessnotebookguide',
    loadChildren: 'app/help/accessnotebookguide/accessnotebookguide.module#AccessNotebookGuideModule',
    canActivate: [AuthorizationGuard]
  }, {
    path: 'help/publickeyguide',
    loadChildren: 'app/help/publickeyguide/publickeyguide.module#PublicKeyGuideModule',
    canActivate: [AuthorizationGuard]
  }];

// {
//     path: 'environment_health_status',
//     loadChildren: 'app/health-status/health-status.module#HealthStatusModule',
//     canActivate: [AuthorizationGuard]
//   },