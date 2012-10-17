package net.madz.download.service;

import java.util.HashMap;

import net.madz.download.LogUtils;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Services;
import net.madz.download.service.services.CreateTaskService;
import net.madz.download.service.services.EchoService;
import net.madz.download.service.services.HelpService;
import net.madz.download.service.services.ListTasksService;
import net.madz.download.service.services.PauseService;

@Services({ EchoService.class, HelpService.class, CreateTaskService.class, PauseService.class, ListTasksService.class })
public class ServiceHub {

    private static ServiceHub serviceHub;
    private final static HashMap<String, IService> servicesRegistry = new HashMap<String, IService>();

    private ServiceHub() {
        Services services = ServiceHub.class.getAnnotation(Services.class);
        Class<? extends IService>[] serviceClass = services.value();
        for ( Class<? extends IService> item : serviceClass ) {
            Command command = item.getAnnotation(Command.class);
            try {
                IService serviceObj = item.newInstance();
                serviceObj.start();
                servicesRegistry.put(command.commandName(), serviceObj);
            } catch (InstantiationException ignored) {
                LogUtils.error(ServiceHub.class, ignored);
            } catch (IllegalAccessException ignored) {
                LogUtils.error(ServiceHub.class, ignored);
            }
        }
    }

    public static synchronized ServiceHub getInstance() {
        if ( null != serviceHub ) {
            return serviceHub;
        }
        serviceHub = new ServiceHub();
        return serviceHub;
    }

    public static IService<?> getService(String commandName) {
        return servicesRegistry.get(commandName);
    }

    public static HashMap<String, IService> getServicesregistry() {
        return servicesRegistry;
    }
}
