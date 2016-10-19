import {Modal} from "./modal.component";
import {CommonModule} from "@angular/common";
import {NgModule, Component} from "@angular/core";

export * from "./modal.component";

@Component({
    selector: "modal-header",
    template: `<ng-content></ng-content>`
})
export class ModalHeader {
}

@Component({
    selector: "modal-content",
    template: `<ng-content></ng-content>`
})
export class ModalContent {
}

@Component({
    selector: "modal-footer",
    template: `<ng-content></ng-content>`
})
export class ModalFooter {
}

@NgModule({
    imports: [
        CommonModule
    ],
    declarations: [
        Modal,
        ModalHeader,
        ModalContent,
        ModalFooter,
    ],
    exports: [
        Modal,
        ModalHeader,
        ModalContent,
        ModalFooter,
    ],
})
export class ModalModule {
}
