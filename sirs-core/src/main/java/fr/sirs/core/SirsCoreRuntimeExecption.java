package fr.sirs.core;


@SuppressWarnings("serial")
public class SirsCoreRuntimeExecption extends RuntimeException {

	public SirsCoreRuntimeExecption(Throwable e) {
		super(e);
	}

    public SirsCoreRuntimeExecption(String message) {
        super(message);
    }

}
