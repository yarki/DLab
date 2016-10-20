import {ResourcesList} from "./resources.component";
import {NgModule, Component} from "@angular/core";
import {CommonModule} from "@angular/common";

export * from "./resources.component";

@NgModule({
  imports: [CommonModule,],
  declarations: [ResourcesList],
  exports: [ResourcesList],
})

export class ResourcesModule { }
