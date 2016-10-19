import {ResourcesList} from "./resources.component";
import {NgModule, Component} from "@angular/core";

export * from "./resources.component";

@NgModule({
    declarations: [
        ResourcesList
    ],
    exports: [
        ResourcesList
    ],
})

export class ResourcesModule { }
