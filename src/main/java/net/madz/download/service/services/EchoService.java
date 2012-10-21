package net.madz.download.service.services;

import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.requests.EchoRequest;
import net.madz.download.service.responses.EchoResponse;

@Command(commandName = "echo", request = EchoRequest.class, arguments = { @Arg(name = "message") }, options = { @Option(shortName = "-l",
        fullName = "--lowerCase", description = "Echo message in lower case.") }, description = "Echo service will echo message.")
public class EchoService implements IService<IServiceRequest> {

    @Override
    public void start() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

    @Override
    public IServiceResponse processRequest(IServiceRequest request) {
        if ( request instanceof EchoRequest ) {
            boolean lowerCase = ( (EchoRequest) request ).isLowercase();
            String result = ( (EchoRequest) request ).getMessage();
            if ( lowerCase ) {
                result = result.toLowerCase();
            }
            return new EchoResponse(result);
        } else {
            return new EchoResponse(request.toString());
        }
    }
}
