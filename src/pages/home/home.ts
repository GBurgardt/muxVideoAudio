import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';

import { Plugins, CameraResultType } from '@capacitor/core';

declare global  {
    interface PluginRegistry {
        CustomNativePlugin?: CustomNativePlugin;
    }
}

interface CustomNativePlugin {
    customCall(): Promise<any>;
}

@Component({
    selector: 'page-home',
    templateUrl: 'home.html'
})
export class HomePage {

    constructor(public navCtrl: NavController) {
        this.test();
    }

    test = () => {
        const { CustomNativePlugin } = Plugins;

        CustomNativePlugin.customCall().then(result => {
            console.log('EL RESULTADO: ');
            console.log(result);
            console.log('FINNNNNN');
        }).catch(err=>{
            console.log(err);
            console.log('Sorry pal, not going to happen');
        });

    }

}
