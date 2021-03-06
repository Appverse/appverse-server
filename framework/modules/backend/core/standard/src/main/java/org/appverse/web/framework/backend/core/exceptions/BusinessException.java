/*
 Copyright (c) 2012 GFT Appverse, S.L., Sociedad Unipersonal.

 This Source Code Form is subject to the terms of the Appverse Public License 
 Version 2.0 (“APL v2.0”). If a copy of the APL was not distributed with this 
 file, You can obtain one at http://www.appverse.mobi/licenses/apl_v2.0.pdf. [^]

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the conditions of the AppVerse Public License v2.0 
 are met.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. EXCEPT IN CASE OF WILLFUL MISCONDUCT OR GROSS NEGLIGENCE, IN NO EVENT
 SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.appverse.web.framework.backend.core.exceptions;

import org.appverse.web.framework.backend.core.AbstractException;

/**
 * Top hierarchy class for business exceptions. Please note it extends
 * {@link AbstractException} and so extends from an unchecked exception
 */
public class BusinessException extends AbstractException {

	private static final long serialVersionUID = 154648249763058696L;

	/**
	 * Default constructor
	 */
	public BusinessException() {
		super();
	}

	/**
	 * Builds a BusinessException with the following parameters
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public BusinessException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Builds a BusinessException with the following parameters
	 * @param message
	 * @param cause
	 */
	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Builds a BusinessException with the following parameters
	 * @param message
	 */
	public BusinessException(String message) {
		super(message);
	}

	/**
	 * Builds a BusinessException with the following parameters
	 * @param cause
	 */
	public BusinessException(Throwable cause) {
		super(cause);
	}
    /**
     * Builds an BusinessException with the following parameters
     * @param code
     */
    public BusinessException(Long code) {
        this();
        setCode(code);
    }
    /**
     * Builds an BusinessException with the following parameters
     * @param code
     * @param cause
     */
    public BusinessException(Long code, Throwable cause) {
        this(cause);
        setCode(code);
    }

    /**
     * Builds an BusinessException with the following parameters
     * @param code
     * @param cause
     */
    public BusinessException(Long code, String message, Throwable cause) {
        this(message, cause);
        setCode(code);
    }

    /**
     * Builds an BusinessException with the following parameters
     * @param code
     * @param message
     */
    public BusinessException(Long code, String message) {
        this(message);
        setCode(code);
    }
}