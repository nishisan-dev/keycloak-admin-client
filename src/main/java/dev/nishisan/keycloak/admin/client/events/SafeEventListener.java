/*
 * Copyright (C) 2025 Lucas Nishimura < lucas at nishisan.dev > 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.nishisan.keycloak.admin.client.events;

import dev.nishisan.keycloak.admin.client.auth.TokenResponseWrapper;

/**
 * make ptotected calls to user listener
 *
 * @author Lucas Nishimura < lucas at nishisan.dev >
 */
public class SafeEventListener implements ITokenEventListener {
    
    private final ITokenEventListener delegate;
    
    public SafeEventListener(ITokenEventListener delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void onTokenIssued(TokenResponseWrapper issuedToken) {
        try {
            delegate.onTokenIssued(issuedToken);
        } catch (Exception ex) {
            // Tratar exceção aqui
            System.err.println("Erro ao processar onTokenIssued: " + ex.getMessage());
        }
    }
    
    @Override
    public void onTokenRefreshed(TokenResponseWrapper refreshedToken) {
        try {
            delegate.onTokenRefreshed(refreshedToken);
        } catch (Exception ex) {
            
        }
    }
    
    @Override
    public String getUniqueName() {
        return delegate.getUniqueName();
    }
    
    
    
}
