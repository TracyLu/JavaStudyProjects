package net.madz.download.service;

import java.util.HashMap;

import net.madz.download.LogUtils;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Services;
import net.madz.download.service.services.CreateTaskService;
import net.madz.download.service.services.EchoService;
import net.madz.download.service.services.HelpService;
import net.madz.download.service.services.ListTasksService;
import net.madz.download.service.services.PauseTaskService;
import net.madz.download.service.services.ResumeTaskService;

@Services({ EchoService.class, HelpService.class, CreateTaskService.class, PauseTaskService.class, ListTasksService.class, ResumeTaskService.class })
public class ServiceHub {

    private static ServiceHub serviceHub;
    private final HashMap<String, IService<? extends IServiceRequest>> servicesRegistry = new HashMap<String, IService<? extends IServiceRequest>>();

    private ServiceHub() {
        Services services = ServiceHub.class.getAnnotation(Services.class);
        Class<? extends IService<? extends IServiceRequest>>[] serviceClass = services.value();
        for ( Class<? extends IService<? extends IServiceRequest>> item : serviceClass ) {
            Command command = item.getAnnotation(Command.class);
            try {
                IService<? extends IServiceRequest> serviceObj = item.newInstance();
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

    public IService<?> getService(String commandName) {
        return servicesRegistry.get(commandName);
    }

    public HashMap<String, IService<? extends IServiceRequest>> getServicesregistry() {
        return servicesRegistry;
    }
}
